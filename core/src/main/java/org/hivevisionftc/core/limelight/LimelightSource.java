package org.hivevisionftc.core.limelight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.hivevisionftc.core.DetectionSource;
import org.hivevisionftc.core.RawDetection;
import org.hivevisionftc.core.VisionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reads the neural detector pipeline on a Limelight 3A. */
public final class LimelightSource implements DetectionSource {
    private final Limelight3A limelight;

    public LimelightSource(Limelight3A limelight, VisionConfig config) {
        this.limelight = limelight;
        limelight.pipelineSwitch(config.pipeline());
        limelight.setPollRateHz(config.pollingRateHz());
        limelight.start();
    }

    @Override
    public List<RawDetection> read() {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return Collections.emptyList();
        long staleness = result.getStaleness();
        List<RawDetection> out = new ArrayList<>();
        for (LLResultTypes.DetectorResult d : result.getDetectorResults()) {
            out.add(new RawDetection(d.getClassName(), -1, d.getTargetXDegrees(),
                    d.getTargetYDegrees(), d.getTargetArea(), d.getConfidence(), staleness));
        }
        return out;
    }

    @Override
    public void close() { limelight.stop(); }
}
