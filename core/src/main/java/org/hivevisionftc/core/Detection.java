package org.hivevisionftc.core;

/** A filtered detection with its position on the field. */
public final class Detection {
    private final BiobuzzClasses ballClass;
    private final double x, y;               // field, inches
    private final double robotX, robotY;     // relative to robot center at capture time
    private final double confidence, area, txDeg, tyDeg;

    Detection(BiobuzzClasses ballClass, double x, double y, double robotX, double robotY,
              double confidence, double area, double txDeg, double tyDeg) {
        this.ballClass = ballClass;
        this.x = x;
        this.y = y;
        this.robotX = robotX;
        this.robotY = robotY;
        this.confidence = confidence;
        this.area = area;
        this.txDeg = txDeg;
        this.tyDeg = tyDeg;
    }

    public BiobuzzClasses getBallClass() { return ballClass; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRobotX() { return robotX; }
    public double getRobotY() { return robotY; }
    public double getConfidence() { return confidence; }
    public double getArea() { return area; }
    public double getTxDeg() { return txDeg; }
    public double getTyDeg() { return tyDeg; }

    /** Distance from robot center when it was seen. */
    public double getDistance() { return Math.hypot(robotX, robotY); }

    /** Angle to the target relative to the robot's heading, radians, CCW positive. */
    public double getBearing() { return Math.atan2(robotY, robotX); }

    public double distanceTo(double fx, double fy) { return Math.hypot(x - fx, y - fy); }

    @Override
    public String toString() {
        return String.format("%s at (%.1f, %.1f) conf %.2f", ballClass, x, y, confidence);
    }
}
