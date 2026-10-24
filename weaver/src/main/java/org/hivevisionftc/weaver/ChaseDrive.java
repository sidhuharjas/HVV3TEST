package org.hivevisionftc.weaver;

import com.skeletonarmy.marrow.weaver.PathResult;

import org.hivevisionftc.core.FieldPose;

/** What BallChaser needs from a drivetrain. PedroBridge.drive(follower) is the real one. */
public interface ChaseDrive {
    FieldPose pose();

    /** Replaces whatever is being followed. */
    void follow(PathResult path);

    /** True while a followed path is still in progress. */
    boolean busy();

    /** Stay where the robot is. */
    void hold();
}
