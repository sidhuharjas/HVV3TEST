---
layout: default
title: Setup
---

# Setup

HiveVision reads a Limelight 3A, turns every detection into an absolute field coordinate and can route around obstacles to reach it. Add two Gradle lines, init the camera, read a detection. That takes about five minutes.

This site is a test copy of the docs styling. The full guide lives in [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs), hosted at [hivevisionftc.github.io/HiveVisionDocs](https://hivevisionftc.github.io/HiveVisionDocs/).

## Prerequisites

* An FTC robot project with Gradle. Your existing `google()` and `mavenCentral()` repositories stay as they are.
* Java 17 to run Gradle and the Android plugin. The library itself is Java 8 bytecode, minSdk 24.
* A Limelight 3A with the Hive Vision model loaded: [Limelight 3A installation](https://hivevisionftc.github.io/HiveVisionDocs/installation/limelight/).
* Pedro Pathing 3 only if you drive with it. Marrow 1.2.2 only if you use `weaver`.

## 1. Add the repository

The artifacts are published to maven on this site. Add one line to what your project already has:

```groovy
repositories {
    // keep everything you already have, then add:
    maven { url = 'https://sidhuharjas.github.io/HVV3TEST/' }
}
```

Kotlin DSL: `maven(url = "https://sidhuharjas.github.io/HVV3TEST/")`.
If repositories live in `settings.gradle.kts` under `dependencyResolutionManagement`, put the same line there.

## 2. Add the dependencies

```groovy
dependencies {
    // add these next to your existing ones:
    implementation 'io.github.sidhuharjas:core:1.0.0'
    implementation 'io.github.sidhuharjas:weaver:1.0.0'   // optional
}
```

The group id is `io.github.sidhuharjas`. The Java packages you import stay `org.hivevisionftc.core` and `org.hivevisionftc.weaver`.

| Coordinate | Resolves from |
| --- | --- |
| `io.github.sidhuharjas:core` | this site |
| `io.github.sidhuharjas:weaver` | this site, pulls in `core` |
| `com.skeletonarmyftc.marrow:core:1.2.2` | Maven Central, needed by `weaver` |
| FTC SDK, Pedro Pathing | your project, `compileOnly` here |

Sync the project. If Gradle reports that it cannot resolve `io.github.sidhuharjas:core`, the repository line is missing or misspelled.

## 3. Quickstart

Every class below comes from `org.hivevisionftc.core` or `org.hivevisionftc.weaver`.

```java
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.hivevisionftc.core.BiobuzzClasses;
import org.hivevisionftc.core.Detection;
import org.hivevisionftc.core.FieldPose;
import org.hivevisionftc.core.HiveVision;
import org.hivevisionftc.core.VisionConfig;
import org.hivevisionftc.weaver.PedroBridge;

// inside your OpMode, after the follower exists
VisionConfig config = new VisionConfig()
        .camPitchDeg(25).camHeight(9.2).ballHeight(3)
        .camXOffset(4).camYOffset(0)
        .minConfidence(0.44).maxStalenessMs(120)
        .pipeline(0).pollingRateHz(100);

HiveVision vision = new HiveVision(hardwareMap.get(Limelight3A.class, "limelight"), config);
vision.setAllowedClasses(BiobuzzClasses.NECTAR_RED, BiobuzzClasses.NECTAR_BLUE);

FieldPose pose = PedroBridge.poseOf(follower);
Detection closest = vision.detections().fetch(pose).sortByDistance().getClosest();

if (closest != null) {
    telemetry.addData("x", closest.getX());
    telemetry.addData("y", closest.getY());
    telemetry.update();
}
```

The camera numbers come from [Calibration](calibration.html). Units are inches and radians in Pedro's frame.

## 4. Real use: chase a ball

```java
import org.hivevisionftc.weaver.BallChaser;
import org.hivevisionftc.weaver.PedroBridge;

BallChaser chaser = new BallChaser(vision, PedroBridge.drive(follower)).stopDistanceIn(8);

// each loop, after follower.update()
if (chaser.update() == BallChaser.State.ARRIVED) {
    // run the intake, then
    chaser.next();
}
```

It locks one ball, plans a path to a spot `stopDistanceIn` short of it, and replans only when the ball moves or the path runs out. States: `SEARCHING`, `CHASING`, `ARRIVED`, `BLOCKED`.

Working OpModes: [AutoBallHuntOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/AutoBallHuntOpMode.java) detects and routes, [ChaseBallOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/ChaseBallOpMode.java) chases.

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `Could not resolve io.github.sidhuharjas:core` | The `maven { url = ... }` line is missing or misspelled. |
| `Could not resolve com.skeletonarmyftc.marrow` | `mavenCentral()` was removed from your repositories, add it back. |
| Compile errors about Pedro classes | Pedro is `compileOnly`, your project has to declare it. |
| No detections | Limelight pipeline type must be `Neural Detector`, `labels.txt` uploaded, the class is in `setAllowedClasses(...)`, confidence not too high. |
| Positions off by a constant | Check [Calibration](calibration.html): `camPitchDeg`, `camHeight`, offsets. |
| Positions spin when the robot turns | Heading must be in radians, not degrees. |
| Detections flicker or lag | Lower `minConfidence`, raise `maxStalenessMs`, make `pipeline` match the Limelight pipeline number. |

## API reference

[Modules](MODULES.html) lists every class in `core` and `weaver` with its purpose. Camera measurements: [Calibration](calibration.html). More examples: [docs/examples](https://github.com/sidhuharjas/HVV3TEST/tree/main/docs/examples). Full documentation: [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs).
