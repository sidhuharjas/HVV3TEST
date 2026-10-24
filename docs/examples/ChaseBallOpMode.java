package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;

import org.hivevisionftc.core.BiobuzzClasses;
import org.hivevisionftc.core.HiveVision;
import org.hivevisionftc.core.VisionConfig;
import org.hivevisionftc.weaver.BallChaser;
import org.hivevisionftc.weaver.PedroBridge;

/** Drive up to the nearest pollen, run your intake, then go for the next one. */
@Autonomous(name = "Chase Ball")
public class ChaseBallOpMode extends LinearOpMode {

    @Override
    public void runOpMode() {
        Follower follower = Constants.createFollower(hardwareMap);
        follower.setPose(new Pose(24, 72, 0));

        VisionConfig config = new VisionConfig()
                .camPitchDeg(25).camHeight(9.2).ballHeight(3)
                .camXOffset(4).camYOffset(0);

        HiveVision vision = new HiveVision(hardwareMap.get(Limelight3A.class, "limelight"), config);
        vision.setAllowedClasses(BiobuzzClasses.POLLEN);

        BallChaser chaser = new BallChaser(vision, PedroBridge.drive(follower))
                .stopDistanceIn(8)                                   // your intake reach
                .avoid(new CircleZone(new Point(72, 72), 18));

        waitForStart();

        while (opModeIsActive()) {
            follower.update();                                       // always, every loop
            BallChaser.State state = chaser.update();

            if (state == BallChaser.State.ARRIVED) {
                // run your intake here; when it has the ball:
                chaser.next();
            }

            telemetry.addData("state", state);
            telemetry.addData("target", chaser.target());
            telemetry.update();
        }
        vision.close();
    }
}
