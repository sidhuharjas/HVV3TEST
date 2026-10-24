package org.hivevisionftc.core;

/** Something that turns a list of detections into a plan. The weaver module provides one. */
public interface Route<R> {
    R plan(DetectionList detections);
}
