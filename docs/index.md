---
layout: default
title: HiveVision
---

# HiveVision

**Camera-based detection, target filtering, field positioning, and Weaver path routing for FTC robots.**

HiveVision turns Limelight 3A neural network detections into absolute field coordinates and generates path routes for autonomous navigation. Built for `POLLEN`, `NECTAR_RED` and `NECTAR_BLUE` using the [Hive Vision SSD detector](https://github.com/hivevisionftc/Hive-Vision), with [Pedro Pathing](https://github.com/Pedro-Pathing/PedroPathing) 3 for driving and [Marrow Weaver](https://marrow.skeletonarmyftc.com/) for routing.

## Modules

| Artifact | Description |
| --- | --- |
| `org.hivevisionftc:core` | Limelight 3A reader, filtering, sorting, tracking, camera-to-field projection |
| `org.hivevisionftc:weaver` | Detections into obstacle-avoiding Marrow Weaver paths, plus Pedro glue |

## Install

`org.hivevisionftc` is not on Maven Central. This site is the maven repository:

```
https://sidhuharjas.github.io/HVV3TEST/
```

Add these lines to what your robot project already has, and keep everything that is already there:

```groovy
repositories {
    // keep what you already have, then add:
    maven { url = 'https://sidhuharjas.github.io/HVV3TEST/' }
}

dependencies {
    // add:
    implementation 'org.hivevisionftc:core:1.0.0'
    implementation 'org.hivevisionftc:weaver:1.0.0'   // optional
}
```

Kotlin DSL: `maven(url = "https://sidhuharjas.github.io/HVV3TEST/")`.

`mavenCentral()` stays, `weaver` pulls `com.skeletonarmyftc.marrow:core` from there. The FTC SDK and Pedro stay `compileOnly`, your project supplies them. Prerequisites, troubleshooting and republishing: [Setup](setup.html).

## Quickstart

```java
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;

import org.hivevisionftc.core.*;
import org.hivevisionftc.weaver.*;

VisionConfig config = new VisionConfig()
        .camPitchDeg(25).camHeight(9.2).ballHeight(3)
        .camXOffset(4).camYOffset(0)
        .minConfidence(0.44).maxStalenessMs(120)
        .pipeline(0).pollingRateHz(100);

HiveVision vision = new HiveVision(hardwareMap.get(Limelight3A.class, "limelight"), config);
vision.setAllowedClasses(BiobuzzClasses.NECTAR_RED, BiobuzzClasses.NECTAR_BLUE);

FieldPose pose = PedroBridge.poseOf(follower);

Detection closest = vision.detections().fetch(pose).sortByDistance().getClosest();

PathResult path = vision.detections().fetch(pose).sortByDistance()
        .with(WeaverRoute.fromFetchPose()
                .avoid(new CircleZone(new Point(72, 72), 18))
                .maxTargets(4));
if (path != null) PedroBridge.follow(follower, path);
```

### Chase a ball

```java
BallChaser chaser = new BallChaser(vision, PedroBridge.drive(follower)).stopDistanceIn(8);

// every loop, after follower.update():
if (chaser.update() == BallChaser.State.ARRIVED) {
    // run your intake, then
    chaser.next();
}
```

It locks one ball, plans a Weaver path to a spot `stopDistanceIn` short of it, and plans again only if the ball moves or the path runs out. States: `SEARCHING`, `CHASING`, `ARRIVED`, `BLOCKED`.

Units are inches and radians, in Pedro's field frame. `CircleZone`, `Point` and `PathResult` are Marrow's. `vision.track(pose)` holds one ball steady in field coordinates so two similar balls don't make the robot shake.

## Docs

- [Setup](setup.html): install, quickstart, troubleshooting
- [Modules](MODULES.html): class reference for core and weaver
- [Calibration](calibration.html): measuring the camera and checking positions
- Examples: [AutoBallHuntOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/AutoBallHuntOpMode.java), [ChaseBallOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/ChaseBallOpMode.java)
- Full documentation: [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs)

## Tests

Two plain `main()` programs in `weaver/src/test/java/org/hivevisionftc/`. `SelfCheck` covers projection, filtering, tracking, routing through the real Weaver and conversion to a Pedro path. `ChaseCheck` runs `BallChaser` against a simulated robot and camera.

## Status

Compiled and run against the actual Pedro Pathing 3.0.2 and Marrow 1.2.2 source, with the Weaver and Pedro code unmodified. Not yet checked: the Limelight reader against the real FTC SDK jars (the SDK isn't public source), the Gradle publishing setup, and a drive on a real robot. The Marrow Maven coordinates (`com.skeletonarmyftc.marrow:core`) come from its build files.
