package org.hivevisionftc.core;

import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.hivevisionftc.core.limelight.LimelightSource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Camera detections in, field coordinates out.
 *
 *   FieldPose pose = new FieldPose(follower.getPose().getX(), ...);
 *   Detection target = vision.detections().fetch(pose).sortByDistance().getClosest();
 */
public final class HiveVision {
    private final DetectionSource source;
    private final VisionConfig config;
    private final Set<BiobuzzClasses> allowed = EnumSet.allOf(BiobuzzClasses.class);
    private final TargetTracker tracker = new TargetTracker();
    private int unknownLabels;

    public HiveVision(Limelight3A limelight, VisionConfig config) {
        this(new LimelightSource(limelight, config), config);
    }

    public HiveVision(DetectionSource source, VisionConfig config) {
        this.source = source;
        this.config = config;
    }

    /** Only these classes survive filtering. Pass nothing to allow all. */
    public HiveVision setAllowedClasses(BiobuzzClasses... classes) {
        allowed.clear();
        if (classes.length == 0) allowed.addAll(EnumSet.allOf(BiobuzzClasses.class));
        else for (BiobuzzClasses c : classes) allowed.add(c);
        return this;
    }

    public DetectionList detections() { return new DetectionList(this); }

    /** Fetch and return the locked target (same ball frame to frame). Null when nothing is in view. */
    public Detection track(FieldPose robotPose) {
        return track(robotPose, System.currentTimeMillis());
    }

    /** Same, with the clock supplied by the caller. */
    public Detection track(FieldPose robotPose, long nowMs) {
        DetectionList list = detections().fetch(robotPose);
        return tracker.update(list, robotPose, nowMs);
    }

    public TargetTracker tracker() { return tracker; }

    /** Boxes the model produced that match no known class. Nonzero after a model swap means update BiobuzzClasses. */
    public int unknownLabelCount() { return unknownLabels; }

    public void close() { source.close(); }

    List<Detection> read(FieldPose pose) {
        List<Detection> out = new ArrayList<>();
        for (RawDetection r : source.read()) {
            if (r.stalenessMs > config.maxStalenessMs) continue;
            BiobuzzClasses cls = BiobuzzClasses.from(r.className, r.classId);
            if (cls == null) { unknownLabels++; continue; }
            if (!allowed.contains(cls) || r.confidence < config.minConfidence) continue;
            double[] robot = Projection.toRobot(r.txDeg, r.tyDeg, config);
            if (robot == null) continue;
            double[] field = Projection.toField(robot, pose);
            out.add(new Detection(cls, field[0], field[1], robot[0], robot[1], r.confidence, r.area, r.txDeg, r.tyDeg));
        }
        return out;
    }
}
