package org.hivevisionftc.core;

/** Robot pose in field coordinates: inches, heading in radians (CCW positive). Same frame as Pedro Pathing. */
public final class FieldPose {
    public final double x;
    public final double y;
    public final double heading;

    public FieldPose(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f deg)", x, y, Math.toDegrees(heading));
    }
}
