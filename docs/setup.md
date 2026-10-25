---
layout: default
title: Setup
---

# Setup

End to end: dependency → camera model → config → first detection. This site is a test copy of the docs style; the full documentation lives in [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs), hosted at [hivevisionftc.github.io/HiveVisionDocs](https://hivevisionftc.github.io/HiveVisionDocs/).

## 1. Add the maven repository

`org.hivevisionftc` does not exist on Maven Central. It is served by this GitHub Pages site, so **add** one repo to what your project already has:

```groovy
repositories {
    // keep everything you already have, then ADD this line:
    maven { url = 'https://sidhuharjas.github.io/HVV3TEST/maven' }
}
```

If your repositories live in `settings.gradle.kts` under `dependencyResolutionManagement`, add it there instead:

```kotlin
maven(url = "https://sidhuharjas.github.io/HVV3TEST/maven")
```

## 2. Add the dependencies

```groovy
dependencies {
    // ADD these, next to your existing dependencies:
    implementation 'org.hivevisionftc:core:1.0.0'
    implementation 'org.hivevisionftc:weaver:1.0.0'   // optional, needs Marrow
}
```

What resolves where:

| Coordinate | Where it comes from |
| --- | --- |
| `org.hivevisionftc:core` | this site (`/maven`) |
| `org.hivevisionftc:weaver` | this site (`/maven`), and it depends on `core` |
| `com.skeletonarmyftc.marrow:core:1.2.2` | Maven Central (already in your `repositories`) |
| FTC SDK, Pedro Pathing | `compileOnly` — your project supplies them |

Nothing needs to be deleted or rewritten: `google()`, `mavenCentral()` and everything else you have stay exactly as they are.

## 3. Limelight + the vision model

The Limelight 3A runs the Hive Vision SSD-MobileNetV2 detector on its own CPU. Getting the `tflite`, `labels.txt` and pipeline configured is covered in the main docs — follow [Limelight 3A installation](https://hivevisionftc.github.io/HiveVisionDocs/installation/limelight/) from [hivevisionftc/HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs). The model files themselves are released in [hivevisionftc/Hive-Vision](https://github.com/hivevisionftc/Hive-Vision).

## 4. Mount and calibrate the camera

Measure `camHeight`, `camPitchDeg`, `camXOffset`, `camYOffset` and `ballHeight`, then verify against a measured position — see [Calibration](calibration.html).

## 5. First detections

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
```

Then routing, ball chasing and the OpMode examples from the [home page](./). Class reference: [Modules](MODULES.html).

## For maintainers: republishing the library

Every published version sits in `docs/maven/` inside this repo, so GitHub Pages *is* the repository. To ship a new version:

1. Bump `version=` in `gradle.properties`.
2. Build and publish to your local maven: `gradle deployLocal` (or the `deployLocal` task in Android Studio). Also set `ftcVersion`, `pedroVersion` and `marrowVersion` to what the robot project uses.
3. Copy the published files in:

   ```
   ~/.m2/repository/org/hivevisionftc/core/<version>/   →  docs/maven/org/hivevisionftc/core/<version>/
   ~/.m2/repository/org/hivevisionftc/weaver/<version>/ →  docs/maven/org/hivevisionftc/weaver/<version>/
   ```

   That is the `.aar`, `.pom` and `.module` for each artifact.
4. Update `<version>` and `lastUpdated` in `docs/maven/org/hivevisionftc/core/maven-metadata.xml` and `.../weaver/maven-metadata.xml`.
5. Commit and push to `sidhuharjas/HVV3TEST`. GitHub Pages builds from `main` → `/docs` (Settings → Pages) and the new version is resolvable within a minute or two.
6. Check it arrived:

   ```
   https://sidhuharjas.github.io/HVV3TEST/maven/org/hivevisionftc/core/<version>/core-<version>.pom
   ```

This site's own docs (`index.md`, `setup.md`, `MODULES.md`, `calibration.md`) are plain markdown in `docs/`, styled after [HiveVisionDocs](https://github.com/hivevisionftc/HiveVisionDocs). Edit those, push, and the pages rebuild the same way.
