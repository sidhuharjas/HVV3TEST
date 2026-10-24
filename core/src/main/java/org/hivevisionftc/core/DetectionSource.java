package org.hivevisionftc.core;

import java.util.List;

/** Where raw detections come from. The Limelight implements this; tests use a fake. */
public interface DetectionSource {
    List<RawDetection> read();

    default void close() {}
}
