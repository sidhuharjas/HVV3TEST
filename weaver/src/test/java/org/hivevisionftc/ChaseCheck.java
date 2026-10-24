package org.hivevisionftc;

import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;

import org.hivevisionftc.core.*;
import org.hivevisionftc.weaver.BallChaser;
import org.hivevisionftc.weaver.ChaseDrive;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Simulated robot and camera: the ball sits at a fixed field spot, the fake camera reports it from wherever the robot is. */
public class ChaseCheck {
    static int failed;
    static void check(String name, boolean ok) {
        System.out.println((ok ? "ok   " : "FAIL ") + name);
        if (!ok) failed++;
    }

    static final VisionConfig cfg = new VisionConfig().camPitchDeg(25).camHeight(9.2).ballHeight(3).camXOffset(4).camYOffset(0);
    static FieldPose pose = new FieldPose(24, 72, 0);
    static double[] ball = {60, 72};
    static boolean visible = true;

    /** Inverse of Projection.toRobot for a point in front of the camera. */
    static RawDetection see() {
        double dx = ball[0] - pose.x, dy = ball[1] - pose.y;
        double fwd = dx * Math.cos(pose.heading) + dy * Math.sin(pose.heading);
        double left = -dx * Math.sin(pose.heading) + dy * Math.cos(pose.heading);
        double F = fwd - 4, right = -left, H = 9.2 - 3, p = Math.toRadians(25);
        if (F <= 0.5) F = 0.5;
        double v = (F * Math.sin(p) - H * Math.cos(p)) / (F * Math.cos(p) + H * Math.sin(p));
        double t = H / (Math.sin(p) - v * Math.cos(p));
        return new RawDetection("yellow_pollen", -1, Math.toDegrees(Math.atan(right / t)), Math.toDegrees(Math.atan(v)), 1.0, 0.9, 0);
    }

    static final DetectionSource cam = new DetectionSource() {
        public List<RawDetection> read() {
            List<RawDetection> l = new ArrayList<RawDetection>();
            if (visible) l.add(see());
            return l;
        }
    };

    static int follows, holds;
    static PathResult lastPath;
    static boolean busy;

    static final ChaseDrive drive = new ChaseDrive() {
        public FieldPose pose() { return pose; }
        public void follow(PathResult p) { follows++; lastPath = p; busy = true; }
        public boolean busy() { return busy; }
        public void hold() { holds++; busy = false; }
    };

    static BallChaser.State step(BallChaser c, long t) throws Exception {
        Method m = BallChaser.class.getDeclaredMethod("update", long.class);
        m.setAccessible(true);
        return (BallChaser.State) m.invoke(c, t);
    }

    static void driveToEndOfPath() {
        Point end = lastPath.getPath().get(1.0);
        pose = new FieldPose(end.getX(), end.getY(), pose.heading);
        busy = false;
    }

    public static void main(String[] args) throws Exception {
        HiveVision vision = new HiveVision(cam, cfg);
        BallChaser chaser = new BallChaser(vision, drive).stopDistanceIn(8);

        long t = 1000;
        check("sanity: sim camera matches projection", Math.abs(
                Projection.toField(Projection.toRobot(see().txDeg, see().tyDeg, cfg), pose)[0] - 60) < 1e-6);

        check("starts SEARCHING", chaser.state() == BallChaser.State.SEARCHING);
        check("first update chases", step(chaser, t) == BallChaser.State.CHASING && follows == 1);

        // small jitter must not cause a replan
        for (int i = 0; i < 10; i++) { t += 200; ball[0] = 60 + (i % 2 == 0 ? 1.0 : -1.0); step(chaser, t); }
        ball[0] = 60;
        check("no replan on jitter", follows == 1);

        // ball rolls well away: replans
        t += 300; ball[0] = 75; ball[1] = 85;
        step(chaser, t);
        check("replans when ball moves", follows == 2);

        // path ends short of the ball: arrives and holds
        driveToEndOfPath();
        t += 200;
        BallChaser.State s = step(chaser, t);
        double d = Math.hypot(ball[0] - pose.x, ball[1] - pose.y);
        check("stops stopDistance short (" + String.format("%.1f", d) + " in)", Math.abs(d - 8) < 2.0);
        check("arrived", s == BallChaser.State.ARRIVED && holds >= 1);
        int holdsBefore = holds, followsBefore = follows;
        t += 200; step(chaser, t);
        check("stays put after arrival", holds == holdsBefore && follows == followsBefore);

        // next ball
        chaser.next();
        pose = new FieldPose(24, 72, 0); ball[0] = 60; ball[1] = 72;
        t += 200;
        check("next() goes after another", step(chaser, t) == BallChaser.State.CHASING);

        // ball vanishes: coast, then give up
        visible = false;
        t += 300;
        check("coasts briefly when ball disappears", step(chaser, t) == BallChaser.State.CHASING);
        t += 2000;
        check("gives up after timeout", step(chaser, t) == BallChaser.State.SEARCHING);

        // ball inside a keep-out zone is blocked
        visible = true;
        BallChaser blocked = new BallChaser(new HiveVision(cam, cfg), drive).avoid(new CircleZone(new Point(60, 72), 6));
        pose = new FieldPose(24, 72, 0);
        t += 300;
        check("ball in zone is BLOCKED", step(blocked, t) == BallChaser.State.BLOCKED);

        // zone in the way: still chases, path stays out
        BallChaser around = new BallChaser(new HiveVision(cam, cfg), drive).avoid(new CircleZone(new Point(41, 72), 3));
        pose = new FieldPose(10, 72, 0); ball[0] = 80; ball[1] = 72;
        t += 300;
        check("routes around a zone in the way", step(around, t) == BallChaser.State.CHASING);
        double min = 1e9;
        for (Point q : lastPath.getPath().sample(40)) min = Math.min(min, q.distanceTo(new Point(41, 72)));
        check("path avoided the zone (min " + String.format("%.1f", min) + " in)", min >= 3);

        System.out.println(failed == 0 ? "all passed" : failed + " FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }
}
