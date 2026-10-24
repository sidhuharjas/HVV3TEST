package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;

import org.hivevisionftc.core.BiobuzzClasses;
import org.hivevisionftc.core.FieldPose;
import org.hivevisionftc.core.HiveVision;
import org.hivevisionftc.core.VisionConfig;
import org.hivevisionftc.weaver.PedroBridge;
import org.hivevisionftc.weaver.WeaverRoute;
import com.pedropathing.math.Pose;

/**
 * Detect, route through the nearest two with Weaver, drive it with Pedro.
 * Constants.createFollower is your team's usual Pedro setup.
 */
@Autonomous(name = "HiveVision Hunt")
public class AutoBallHuntOpMode extends LinearOpMode {

    @Override
    public void runOpMode() {
        Follower follower = Constants.createFollower(hardwareMap);
        follower.setPose(new Pose(24, 72, 0));

        VisionConfig config = new VisionConfig()
                .camPitchDeg(25).camHeight(9.2).ballHeight(3)
                .camXOffset(4).camYOffset(0)
                .minConfidence(0.44).maxStalenessMs(120)
                .pipeline(0);

        HiveVision vision = new HiveVision(hardwareMap.get(Limelight3A.class, "limelight"), config);
        vision.setAllowedClasses(BiobuzzClasses.POLLEN, BiobuzzClasses.NECTAR_RED);

        waitForStart();

        while (opModeIsActive()) {
            follower.update();
            FieldPose pose = PedroBridge.poseOf(follower);

            if (!follower.isBusy()) {
                PathResult path = vision.detections()
                        .fetch(pose)
                        .clampYDetections(2, 142)
                        .sortByDistance()
                        .with(WeaverRoute.fromFetchPose()
                                .avoid(new CircleZone(new Point(72, 72), 18))
                                .maxTargets(2));
                if (path != null) PedroBridge.follow(follower, path);
            }

            telemetry.addData("pose", pose);
            telemetry.addData("busy", follower.isBusy());
            telemetry.addData("unknown labels", vision.unknownLabelCount());
            telemetry.update();
        }
        vision.close();
    }
}
