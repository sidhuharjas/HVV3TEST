package org.hivevisionftc;

import com.pedropathing.follower.Follower;
import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.weaver.PedroPathingConverter;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;

import org.hivevisionftc.core.*;
import org.hivevisionftc.weaver.WeaverRoute;

import java.util.ArrayList;
import java.util.List;

/** Plain main(), no JUnit: javac + java. Uses the real Marrow Weaver and Pedro 3 paths. */
public class SelfCheck {
    static int failed;
    static void check(String name, boolean ok) {
        System.out.println((ok ? "ok   " : "FAIL ") + name);
        if (!ok) failed++;
    }
    static boolean near(double a, double b) { return Math.abs(a - b) < 1e-6; }

    static final List<RawDetection> frame = new ArrayList<>();
    static final DetectionSource src = new DetectionSource() {
        public List<RawDetection> read() { return frame; }
    };

    static RawDetection raw(String cls, double tx, double ty, double area, double conf, long stale) {
        return new RawDetection(cls, -1, tx, ty, area, conf, stale);
    }

    public static void main(String[] a) {
        VisionConfig cfg = new VisionConfig().camPitchDeg(25).camHeight(9.2).ballHeight(3).camXOffset(4).camYOffset(0);

        // projection
        double[] r0 = Projection.toRobot(0, 0, cfg);
        check("center pixel distance", near(r0[0], 6.2 / Math.tan(Math.toRadians(25)) + 4) && near(r0[1], 0));
        double[] r1 = Projection.toRobot(10, 0, cfg), r2 = Projection.toRobot(-10, 0, cfg);
        check("right of crosshair is negative y", r1[1] < 0);
        check("left/right symmetric", near(r1[1], -r2[1]) && near(r1[0], r2[0]));
        check("lower in image is closer", Projection.toRobot(0, -10, cfg)[0] < r0[0]);
        check("above horizon rejected", Projection.toRobot(0, 70, cfg) == null);
        double[] f = Projection.toField(new double[] {10, 0}, new FieldPose(72, 72, Math.PI / 2));
        check("toField rotates", near(f[0], 72) && near(f[1], 82));

        // filtering
        HiveVision vision = new HiveVision(src, cfg);
        FieldPose pose = new FieldPose(24, 24, 0);
        frame.clear();
        frame.add(raw("yellow_pollen", 0, 0, 2.0, 0.90, 10));
        frame.add(raw("red_nectar", 5, -3, 1.0, 0.70, 10));
        frame.add(raw("blue_nectar", 0, 0, 0.5, 0.20, 10));
        frame.add(raw("yellow_pollen", 0, 0, 0.5, 0.90, 500));
        frame.add(raw("banana", 0, 0, 0.5, 0.90, 10));
        DetectionList list = vision.detections().fetch(pose);
        check("confidence/staleness/unknown filtered", list.size() == 2);
        check("unknown counted", vision.unknownLabelCount() == 1);
        vision.setAllowedClasses(BiobuzzClasses.NECTAR_RED);
        check("allowed classes", vision.detections().fetch(pose).size() == 1);
        vision.setAllowedClasses();
        list = vision.detections().fetch(pose).sortByDistance();
        check("sorted nearest first", list.get(0).getDistance() <= list.get(1).getDistance());
        check("getBiggest uses area", list.getBiggest().getBallClass() == BiobuzzClasses.POLLEN);
        check("clampY drops out-of-field", vision.detections().fetch(new FieldPose(24, 140, Math.PI / 2))
                .clampYDetections(0, 144).size() < 2);

        // tracker
        HiveVision v2 = new HiveVision(src, cfg);
        FieldPose rp = new FieldPose(0, 0, 0);
        frame.clear();
        frame.add(raw("yellow_pollen", -8, -5, 1, 0.9, 0));
        frame.add(raw("yellow_pollen", 8, -5, 1, 0.9, 0));
        Detection first = v2.track(rp);
        int flips = 0;
        for (int i = 0; i < 40; i++) {
            frame.clear();
            double wob = (i % 2 == 0) ? -0.4 : 0.4;
            frame.add(raw("yellow_pollen", -8 + wob, -5, 1, 0.9, 0));
            frame.add(raw("yellow_pollen", 8 - wob, -5, 1, 0.9, 0));
            if (v2.track(rp).distanceTo(first.getX(), first.getY()) > 8) flips++;
        }
        check("no flip-flop between similar targets", flips == 0);
        frame.clear();
        check("holds briefly with nothing in view", v2.track(rp) != null && v2.tracker().isHolding());

        // Weaver (real Marrow)
        frame.clear();
        frame.add(raw("yellow_pollen", 0, 16, 2.0, 0.9, 0));
        HiveVision v3 = new HiveVision(src, cfg);
        FieldPose start = new FieldPose(24, 72, 0);
        DetectionList l3 = v3.detections().fetch(start).sortByDistance();
        PathResult straight = l3.with(WeaverRoute.fromFetchPose());
        check("weaver returns a path", straight != null && straight.getVisitedTargets().size() == 1);

        Detection t = l3.getClosest();
        Point mid = new Point((start.x + t.getX()) / 2, 72);
        CircleZone zone = new CircleZone(mid, 3);
        PathResult around = l3.with(WeaverRoute.fromFetchPose().avoid(zone));
        boolean clear = around != null;
        double minDist = 1e9;
        if (around != null) for (Point p : around.getPath().sample(40)) minDist = Math.min(minDist, p.distanceTo(mid));
        check("path stays outside the zone (min " + String.format("%.1f", minDist) + " in)", clear && minDist >= 3);
        check("detour is longer than straight",
                around != null && around.getPath().approxLength(40) > straight.getPath().approxLength(40));

        CircleZone onBall = new CircleZone(new Point(t.getX(), t.getY()), 3);
        check("target inside zone gives null", l3.with(WeaverRoute.fromFetchPose().avoid(onBall)) == null);

        frame.clear();
        frame.add(raw("yellow_pollen", 0, 0, 2.0, 0.9, 0));
        frame.add(raw("red_nectar", 12, -2, 1.0, 0.9, 0));
        frame.add(raw("blue_nectar", -12, -2, 1.0, 0.9, 0));
        DetectionList l4 = v3.detections().fetch(start).sortByDistance();
        check("maxTargets", l4.with(WeaverRoute.fromFetchPose().maxTargets(2)).getVisitedTargets().size() == 2);
        check("all targets visited by default", l4.with(WeaverRoute.fromFetchPose()).getVisitedTargets().size() == 3);

        // Pedro 3 conversion of the real result
        check("converts to a Pedro path", PedroPathingConverter.toPath(l4.with(WeaverRoute.fromFetchPose())) != null);

        System.out.println(failed == 0 ? "all passed" : failed + " FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }
}
