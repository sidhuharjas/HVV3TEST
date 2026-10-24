package org.hivevisionftc.weaver;

import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.weaver.Weaver;
import com.skeletonarmy.marrow.zones.Point;
import com.skeletonarmy.marrow.zones.Zone;

import org.hivevisionftc.core.Detection;
import org.hivevisionftc.core.FieldPose;
import org.hivevisionftc.core.HiveVision;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives up to a ball and stops. Call update() every loop.
 *
 * It locks one ball with HiveVision.track, plans a Weaver path to a spot
 * stopDistanceIn short of it, and plans again only when the ball has moved
 * or the path ran out. It never touches your intake: when state() is ARRIVED,
 * run it yourself, then call next() to go for the following ball.
 */
public final class BallChaser {

    public enum State {
        /** Nothing in view. */
        SEARCHING,
        /** Following a path to the ball. */
        CHASING,
        /** Close enough. The robot is holding position. */
        ARRIVED,
        /** A ball is in view but Weaver found no way to it (inside a zone, say). */
        BLOCKED
    }

    private final HiveVision vision;
    private final ChaseDrive drive;
    private final List<Zone> zones = new ArrayList<>();

    private double stopDistanceIn = 8;
    private double arriveToleranceIn = 1.5;
    private double replanDistanceIn = 6;
    private long minReplanMs = 150;
    private long lostTimeoutMs = 500;

    private State state = State.SEARCHING;
    private Point plannedFor;
    private long lastPlanMs;
    private long lastSeenMs;
    private Detection target;

    public BallChaser(HiveVision vision, ChaseDrive drive) {
        this.vision = vision;
        this.drive = drive;
    }

    /** How far from the ball's center to stop. Set it to your intake reach. */
    public BallChaser stopDistanceIn(double v) { stopDistanceIn = v; return this; }
    /** Counts as arrived within this much of the stop distance. */
    public BallChaser arriveToleranceIn(double v) { arriveToleranceIn = v; return this; }
    /** Plan again when the ball has moved this far from where the current path aims. */
    public BallChaser replanDistanceIn(double v) { replanDistanceIn = v; return this; }
    public BallChaser minReplanMs(long v) { minReplanMs = v; return this; }
    /** How long a ball can be missing before the chase gives up and goes back to SEARCHING. */
    public BallChaser lostTimeoutMs(long v) { lostTimeoutMs = v; return this; }
    public BallChaser avoid(Zone zone) { zones.add(zone); return this; }

    public State state() { return state; }

    /** The ball currently being chased, or null. */
    public Detection target() { return target; }

    /** Forget the current ball and look for the next one. Call after the intake has run. */
    public void next() {
        vision.tracker().reset();
        target = null;
        plannedFor = null;
        state = State.SEARCHING;
    }

    public State update() { return update(System.currentTimeMillis()); }

    State update(long nowMs) {
        if (state == State.ARRIVED) return state;      // stay put until next()

        FieldPose pose = drive.pose();
        Detection ball = vision.track(pose, nowMs);

        if (ball == null) {
            target = null;
            if (state == State.CHASING && nowMs - lastSeenMs > lostTimeoutMs) {
                drive.hold();
                plannedFor = null;
                state = State.SEARCHING;
            } else if (state != State.CHASING) {
                state = State.SEARCHING;
            }
            return state;
        }

        target = ball;
        boolean stale = vision.tracker().isHolding();
        if (!stale) lastSeenMs = nowMs;

        double dist = ball.distanceTo(pose.x, pose.y);
        if (dist <= stopDistanceIn + arriveToleranceIn) {
            drive.hold();
            state = State.ARRIVED;
            return state;
        }

        if (stale) return state;                        // coast on the path we have

        Point aim = approachPoint(ball, pose);
        boolean moved = plannedFor == null || aim.distanceTo(plannedFor) > replanDistanceIn;
        boolean pathDone = state == State.CHASING && !drive.busy();
        boolean retryBlocked = state == State.BLOCKED;
        if ((moved || pathDone || retryBlocked) && nowMs - lastPlanMs >= minReplanMs) {
            lastPlanMs = nowMs;
            PathResult path = plan(pose, aim);
            if (path == null) {
                state = State.BLOCKED;
                plannedFor = null;
            } else {
                drive.follow(path);
                plannedFor = aim;
                state = State.CHASING;
            }
        } else if (state == State.SEARCHING) {
            state = State.CHASING;
        }
        return state;
    }

    private Point approachPoint(Detection ball, FieldPose pose) {
        double dx = pose.x - ball.getX(), dy = pose.y - ball.getY();
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) return new Point(ball.getX(), ball.getY());
        return new Point(ball.getX() + dx / len * stopDistanceIn, ball.getY() + dy / len * stopDistanceIn);
    }

    private PathResult plan(FieldPose pose, Point aim) {
        PathResult result = Weaver.builder()
                .start(new Point(pose.x, pose.y, pose.heading))
                .targets(aim)
                .obstacles(zones)
                .generate();
        return result.getVisitedTargets().isEmpty() ? null : result;
    }
}
