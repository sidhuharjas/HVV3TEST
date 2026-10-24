package org.hivevisionftc.weaver;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.weaver.PedroPathingConverter;

import org.hivevisionftc.core.FieldPose;

/** Small glue between HiveVision, Marrow and a Pedro 3 Follower. */
public final class PedroBridge {
    private PedroBridge() {}

    /** The follower's current pose in the form HiveVision wants. */
    public static FieldPose poseOf(Follower follower) {
        Pose p = follower.pose();
        return new FieldPose(p.x(), p.y(), p.heading());
    }

    /** Wraps a follower so BallChaser can drive it. */
    public static ChaseDrive drive(final Follower follower) {
        return new ChaseDrive() {
            @Override public FieldPose pose() { return poseOf(follower); }
            @Override public void follow(PathResult path) { PedroBridge.follow(follower, path); }
            @Override public boolean busy() { return follower.isBusy(); }
            @Override public void hold() { follower.hold(follower.pose()); }
        };
    }

    /** Starts following a Weaver result. */
    public static void follow(Follower follower, PathResult result) {
        follower.follow(PedroPathingConverter.toPath(result));
    }
}
