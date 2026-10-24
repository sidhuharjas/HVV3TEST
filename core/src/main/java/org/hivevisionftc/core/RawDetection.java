package org.hivevisionftc.core;

/** One box straight from the camera, before any filtering. */
public final class RawDetection {
    public final String className;
    public final int classId;
    public final double txDeg;   // positive = right of crosshair
    public final double tyDeg;   // positive = above crosshair
    public final double area;
    public final double confidence;
    public final long stalenessMs;

    public RawDetection(String className, int classId, double txDeg, double tyDeg,
                        double area, double confidence, long stalenessMs) {
        this.className = className;
        this.classId = classId;
        this.txDeg = txDeg;
        this.tyDeg = tyDeg;
        this.area = area;
        this.confidence = confidence;
        this.stalenessMs = stalenessMs;
    }
}
