---
layout: default
title: HiveVision
---

# HiveVision

**Camera-based detection, target filtering, field positioning, and Weaver path routing for FTC robots.**

HiveVision turns Limelight 3A neural network detections into absolute field coordinates and generates path routes for autonomous navigation. Built for `POLLEN`, `NECTAR_RED` and `NECTAR_BLUE` using the Hive Vision SSD detector, with [Pedro Pathing](https://github.com/Pedro-Pathing/PedroPathing) 3 for driving and [Marrow Weaver](https://marrow.skeletonarmyftc.com/) for routing.

[Modules](MODULES.html) · [Calibration](calibration.html) · [GitHub](https://github.com/sidhuharjas/HVV3TEST)

## Modules

| Artifact | Description |
| --- | --- |
| `org.hivevisionftc:core` | Limelight 3A reader, filtering, sorting, tracking, camera-to-field projection |
| `org.hivevisionftc:weaver` | Detections into obstacle-avoiding Marrow Weaver paths, plus Pedro glue |

## Install

In the repo: `gradle deployLocal` (or the `deployLocal` task in Android Studio). Then in the robot project:

```groovy
repositories { mavenLocal() }
dependencies {
    implementation 'org.hivevisionftc:core:1.0.0'
    implementation 'org.hivevisionftc:weaver:1.0.0'   // optional
}
```

The built library lives on the computer that built it. Set `ftcVersion`, `pedroVersion` and `marrowVersion` in `gradle.properties` to what the robot project uses.

## Quickstart

```java
VisionConfig config = new VisionConfig()
        .camPitchDeg(25).camHeight(9.2).ballHeight(3)
        .camXOffset(4).camYOffset(0)
        .minConfidence(0.44).maxStalenessMs(120)
        .pipeline(0).pollingRateHz(100);

HiveVision vision = new HiveVision(hardwareMap.get(Limelight3A.class, "limelight"), config);
vision.setAllowedClasses(BiobuzzClasses.NECTAR_RED, BiobuzzClasses.NECTAR_BLUE);

FieldPose pose = PedroBridge.poseOf(follower);

Detection target = vision.detections().fetch(pose).sortByDistance().getClosest();

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

- [Modules](MODULES.html) — class reference for core and weaver
- [Calibration](calibration.html) — measuring the camera and checking positions
- Examples: [AutoBallHuntOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/AutoBallHuntOpMode.java), [ChaseBallOpMode.java](https://github.com/sidhuharjas/HVV3TEST/blob/main/docs/examples/ChaseBallOpMode.java)

## Tests

Two plain `main()` programs in `weaver/src/test/java/org/hivevisionftc/`. `SelfCheck` covers projection, filtering, tracking, routing through the real Weaver and conversion to a Pedro path. `ChaseCheck` runs `BallChaser` against a simulated robot and camera.

## Status

Compiled and run against the actual Pedro Pathing 3.0.2 and Marrow 1.2.2 source, with the Weaver and Pedro code unmodified. Not yet checked: the Limelight reader against the real FTC SDK jars (the SDK isn't public source), the Gradle publishing setup, and a drive on a real robot. The Marrow Maven coordinates (`com.skeletonarmyftc.marrow:core`) come from its build files.
