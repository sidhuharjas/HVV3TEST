package org.hivevisionftc.weaver;

import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.weaver.Weaver;
import com.skeletonarmy.marrow.weaver.WeaverBuilder;
import com.skeletonarmy.marrow.zones.Point;
import com.skeletonarmy.marrow.zones.Zone;

import org.hivevisionftc.core.Detection;
import org.hivevisionftc.core.DetectionList;
import org.hivevisionftc.core.FieldPose;
import org.hivevisionftc.core.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Hands the detections to Marrow's Weaver as targets and returns its PathResult.
 * Robot size and clearance come from Weaver's own config (Weaver.setConfig).
 * Returns null when no target can be reached.
 */
public final class WeaverRoute implements Route<PathResult> {
    private final FieldPose fixedStart;
    private final List<Zone> zones = new ArrayList<>();
    private int maxTargets = 0;
    private boolean keepOrder;
    private Point end;

    private WeaverRoute(FieldPose start) { this.fixedStart = start; }

    /** Start from the pose given to fetch(). */
    public static WeaverRoute fromFetchPose() { return new WeaverRoute(null); }

    public static WeaverRoute fromPose(FieldPose start) { return new WeaverRoute(start); }

    public WeaverRoute avoid(Zone zone) { zones.add(zone); return this; }

    /** Visit at most n targets. 0 or less means all. */
    public WeaverRoute maxTargets(int n) { maxTargets = n; return this; }

    /** Visit in list order instead of letting Weaver pick the order. */
    public WeaverRoute ordered() { keepOrder = true; return this; }

    /** Finish at this point after the last target. */
    public WeaverRoute endAt(Point destination) { end = destination; return this; }

    @Override
    public PathResult plan(DetectionList detections) {
        FieldPose start = fixedStart != null ? fixedStart : detections.fetchPose();
        if (start == null) throw new IllegalStateException("No start pose: call fetch(pose) or use fromPose(pose)");
        if (detections.isEmpty()) return null;

        List<Point> targets = new ArrayList<>();
        for (Detection d : detections) targets.add(new Point(d.getX(), d.getY()));

        WeaverBuilder builder = Weaver.builder()
                .start(new Point(start.x, start.y, start.heading))
                .targets(targets)
                .obstacles(zones);
        if (keepOrder) builder.ordered();
        if (maxTargets > 0) builder.maxTargets(maxTargets);
        if (end != null) builder.end(end);

        PathResult result = builder.generate();
        return result.getVisitedTargets().isEmpty() ? null : result;
    }
}
