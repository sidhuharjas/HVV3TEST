package org.hivevisionftc.core;

/**
 * Sticks with one target in field coordinates. Because the position is on the
 * field and not in the image, it stays put while the robot turns or drives.
 */
public final class TargetTracker {
    private double matchRadiusIn = 8;
    private double switchMarginIn = 8;
    private long holdMs = 250;

    private Detection current;
    private long lastSeenMs;
    private boolean holding;

    public TargetTracker matchRadiusIn(double v) { matchRadiusIn = v; return this; }
    public TargetTracker switchMarginIn(double v) { switchMarginIn = v; return this; }
    public TargetTracker holdMs(long v) { holdMs = v; return this; }

    public Detection current() { return current; }
    public boolean isHolding() { return holding; }
    public void reset() { current = null; holding = false; }

    public Detection update(DetectionList list, FieldPose robot, long nowMs) {
        if (list.isEmpty()) {
            holding = current != null;
            if (current != null && nowMs - lastSeenMs > holdMs) reset();
            return current;
        }

        Detection nearest = null;
        for (Detection d : list) {
            if (nearest == null || d.distanceTo(robot.x, robot.y) < nearest.distanceTo(robot.x, robot.y)) nearest = d;
        }
        if (current == null) return take(nearest, nowMs);

        Detection same = null;
        double best = matchRadiusIn;
        for (Detection d : list) {
            double dist = d.distanceTo(current.getX(), current.getY());
            if (dist <= best) { same = d; best = dist; }
        }

        if (same == null) {
            if (nowMs - lastSeenMs > holdMs) return take(nearest, nowMs);
            holding = true;
            return current;
        }

        boolean clearlyCloser = nearest != same
                && nearest.distanceTo(robot.x, robot.y) + switchMarginIn < same.distanceTo(robot.x, robot.y);
        return take(clearlyCloser ? nearest : same, nowMs);
    }

    private Detection take(Detection d, long nowMs) {
        current = d;
        lastSeenMs = nowMs;
        holding = false;
        return d;
    }
}
