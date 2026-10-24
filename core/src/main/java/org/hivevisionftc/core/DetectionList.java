package org.hivevisionftc.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** Chainable view of the latest detections. Methods change this list and return it. */
public final class DetectionList implements Iterable<Detection> {
    private final HiveVision owner;
    private List<Detection> items = new ArrayList<>();
    private FieldPose pose;

    DetectionList(HiveVision owner) { this.owner = owner; }

    /** Reads the camera and projects what it sees onto the field using this robot pose. */
    public DetectionList fetch(FieldPose robotPose) {
        this.pose = robotPose;
        this.items = new ArrayList<>(owner.read(robotPose));
        return this;
    }

    /** The pose passed to the last fetch, or null. */
    public FieldPose fetchPose() { return pose; }

    public DetectionList sortByDistance() {
        if (pose == null) throw new IllegalStateException("fetch(pose) first");
        final FieldPose p = pose;
        Collections.sort(items, new Comparator<Detection>() {
            @Override public int compare(Detection a, Detection b) {
                return Double.compare(a.distanceTo(p.x, p.y), b.distanceTo(p.x, p.y));
            }
        });
        return this;
    }

    public DetectionList sortByConfidence() {
        Collections.sort(items, new Comparator<Detection>() {
            @Override public int compare(Detection a, Detection b) {
                return Double.compare(b.getConfidence(), a.getConfidence());
            }
        });
        return this;
    }

    /** Drops anything whose field Y is outside [minY, maxY], e.g. things past the wall. */
    public DetectionList clampYDetections(double minY, double maxY) {
        List<Detection> kept = new ArrayList<>();
        for (Detection d : items) if (d.getY() >= minY && d.getY() <= maxY) kept.add(d);
        items = kept;
        return this;
    }

    public DetectionList clampXDetections(double minX, double maxX) {
        List<Detection> kept = new ArrayList<>();
        for (Detection d : items) if (d.getX() >= minX && d.getX() <= maxX) kept.add(d);
        items = kept;
        return this;
    }

    public DetectionList only(BiobuzzClasses... classes) {
        List<Detection> kept = new ArrayList<>();
        for (Detection d : items) for (BiobuzzClasses c : classes) if (d.getBallClass() == c) { kept.add(d); break; }
        items = kept;
        return this;
    }

    public DetectionList limit(int n) {
        if (items.size() > n) items = new ArrayList<>(items.subList(0, n));
        return this;
    }

    /** First item. Call sortByDistance() first if you want the nearest. */
    public Detection getClosest() { return items.isEmpty() ? null : items.get(0); }

    /** Largest on screen, which is usually the nearest regardless of the current sort. */
    public Detection getBiggest() {
        Detection best = null;
        for (Detection d : items) if (best == null || d.getArea() > best.getArea()) best = d;
        return best;
    }

    public <R> R with(Route<R> route) { return route.plan(this); }

    public int size() { return items.size(); }
    public boolean isEmpty() { return items.isEmpty(); }
    public Detection get(int i) { return items.get(i); }
    public List<Detection> toList() { return Collections.unmodifiableList(items); }
    @Override public Iterator<Detection> iterator() { return toList().iterator(); }
}
