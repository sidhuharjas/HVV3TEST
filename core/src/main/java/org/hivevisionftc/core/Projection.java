package org.hivevisionftc.core;

/**
 * Turns Limelight tx/ty into a spot on the floor. The ray through the pixel is
 * rotated by the camera pitch and intersected with the plane at ball-center height.
 */
public final class Projection {
    private Projection() {}

    /** Position relative to robot center (x forward, y left), or null if the ray never reaches the plane. */
    public static double[] toRobot(double txDeg, double tyDeg, VisionConfig c) {
        double u = Math.tan(Math.toRadians(txDeg));
        double v = Math.tan(Math.toRadians(tyDeg));
        double p = Math.toRadians(c.camPitchDeg);
        double drop = Math.sin(p) - v * Math.cos(p);          // how fast the ray falls per unit travel
        if (drop <= 1e-6) return null;
        double t = (c.camHeight - c.ballHeight) / drop;
        double forward = t * (Math.cos(p) + v * Math.sin(p));
        if (forward <= 0) return null;
        return new double[] { forward + c.camXOffset, -t * u + c.camYOffset };
    }

    public static double[] toField(double[] robot, FieldPose pose) {
        double cos = Math.cos(pose.heading), sin = Math.sin(pose.heading);
        return new double[] {
            pose.x + robot[0] * cos - robot[1] * sin,
            pose.y + robot[0] * sin + robot[1] * cos
        };
    }
}
