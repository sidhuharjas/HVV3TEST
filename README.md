# HiveVision

Camera-based detection, target filtering, field positioning, and Weaver path routing library for FTC robots.

HiveVision turns Limelight 3A neural network detections into absolute field coordinates and generates path routes for autonomous navigation. Built for `POLLEN`, `NECTAR_RED` and `NECTAR_BLUE` using the Hive Vision SSD detector, with [Pedro Pathing](https://github.com/Pedro-Pathing/PedroPathing) 3 for driving and [Marrow Weaver](https://marrow.skeletonarmyftc.com/) for routing.

## Modules

| Artifact | Description |
| --- | --- |
| `org.hivevisionftc:core` | Limelight 3A reader, filtering, sorting, tracking, camera-to-field projection |
| `org.hivevisionftc:weaver` | Detections into obstacle-avoiding Marrow Weaver paths, plus Pedro glue |

## Install

`org.hivevisionftc` is not on Maven Central — it is hosted on the project's GitHub Pages as a maven repository. **Add** these to what the robot project already has (don't replace its existing `repositories`/`dependencies`):

```groovy
repositories {
    // keep what you already have, then ADD:
    maven { url = 'https://sidhuharjas.github.io/HVV3TEST/maven' }
}

dependencies {
    // ADD:
    implementation 'org.hivevisionftc:core:1.0.0'
    implementation 'org.hivevisionftc:weaver:1.0.0'   // optional
}
```

`mavenCentral()` stays — `weaver` pulls `com.skeletonarmyftc.marrow:core` from Central. To build and publish a version yourself, see [docs/setup.md](docs/setup.md) (repo → `gradle deployLocal` → copy into `docs/maven/` → push). Set `ftcVersion`, `pedroVersion` and `marrowVersion` in `gradle.properties` to what the robot project uses.

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

It locks one ball, plans a Weaver path to a spot `stopDistanceIn` short of it, and plans again only if the ball moves or the path runs out. States: `SEARCHING`, `CHASING`, `ARRIVED`, `BLOCKED`. See [docs/examples/ChaseBallOpMode.java](docs/examples/ChaseBallOpMode.java).

Units are inches and radians, in Pedro's field frame. `CircleZone`, `Point` and `PathResult` are Marrow's. `vision.track(pose)` holds one ball steady in field coordinates so two similar balls don't make the robot shake.

## Docs

- [sidhuharjas.github.io/HVV3TEST](https://sidhuharjas.github.io/HVV3TEST/) this site
- [docs/setup.md](docs/setup.md) end to end: maven repo, model, calibration, republishing
- [docs/MODULES.md](docs/MODULES.md) class reference
- [docs/calibration.md](docs/calibration.md) measuring the camera and checking positions
- [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs) full documentation
- [docs/examples/AutoBallHuntOpMode.java](docs/examples/AutoBallHuntOpMode.java) detect, route, drive

## Tests

Two plain `main()` programs in `weaver/src/test/java/org/hivevisionftc/`. `SelfCheck` covers projection, filtering, tracking, routing through the real Weaver and conversion to a Pedro path. `ChaseCheck` runs `BallChaser` against a simulated robot and camera.

## Status

Compiled and run against the actual Pedro Pathing 3.0.2 and Marrow 1.2.2 source, with the Weaver and Pedro code unmodified. Not yet checked: the Limelight reader against the real FTC SDK jars (the SDK isn't public source), the Gradle publishing setup, and a drive on a real robot. The Marrow Maven coordinates (`com.skeletonarmyftc.marrow:core`) come from its build files.
