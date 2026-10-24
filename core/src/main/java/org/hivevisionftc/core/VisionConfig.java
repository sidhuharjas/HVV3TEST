package org.hivevisionftc.core;

/** Camera geometry and thresholds. Lengths are inches, angles degrees. */
public final class VisionConfig {
    double camPitchDeg = 25.0;
    double camHeight = 9.2;
    double ballHeight = 3.0;
    double camXOffset = 4.0;
    double camYOffset = 0.0;
    double minConfidence = 0.44;
    long maxStalenessMs = 120;
    int pipeline = 0;
    int pollingRateHz = 100;

    /** Downward tilt of the optic axis, degrees below horizontal. */
    public VisionConfig camPitchDeg(double v) { camPitchDeg = v; return this; }
    /** Floor to lens center. */
    public VisionConfig camHeight(double v) { camHeight = v; return this; }
    /** Floor to center of a game element. */
    public VisionConfig ballHeight(double v) { ballHeight = v; return this; }
    /** Lens position ahead of robot center. */
    public VisionConfig camXOffset(double v) { camXOffset = v; return this; }
    /** Lens position left of robot center (negative = right). */
    public VisionConfig camYOffset(double v) { camYOffset = v; return this; }
    public VisionConfig minConfidence(double v) { minConfidence = v; return this; }
    public VisionConfig maxStalenessMs(long v) { maxStalenessMs = v; return this; }
    public VisionConfig pipeline(int v) { pipeline = v; return this; }
    public VisionConfig pollingRateHz(int v) { pollingRateHz = v; return this; }

    public int pipeline() { return pipeline; }
    public int pollingRateHz() { return pollingRateHz; }
}
