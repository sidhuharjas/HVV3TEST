# Modules

## core (`org.hivevisionftc:core`)

Depends on the FTC SDK (compileOnly). No Pedro or Marrow.

| Class | Purpose |
| --- | --- |
| `HiveVision` | Entry point. `detections()`, `track(pose)`, `setAllowedClasses(...)`, `unknownLabelCount()`, `close()` |
| `VisionConfig` | Camera geometry, thresholds, Limelight pipeline and poll rate |
| `FieldPose` | Robot pose: inches, radians |
| `Detection` | Filtered detection: class, field x/y, robot-frame x/y, distance, bearing, confidence, area |
| `DetectionList` | Chainable: `fetch`, `sortByDistance`, `sortByConfidence`, `clampXDetections`, `clampYDetections`, `only`, `limit`, `getClosest`, `getBiggest`, `with(Route)` |
| `TargetTracker` | Keeps one ball selected frame to frame in field coordinates |
| `BiobuzzClasses` | `POLLEN` (yellow_pollen), `NECTAR_RED` (red_nectar), `NECTAR_BLUE` (blue_nectar) |
| `Projection` | camera angle to robot frame to field frame |
| `DetectionSource`, `RawDetection` | Seam for non-Limelight sources and for tests |
| `limelight.LimelightSource` | Reads the detector pipeline of a Limelight 3A |
| `Route<R>` | Interface used by `DetectionList.with(...)` |

## weaver (`org.hivevisionftc:weaver`)

Depends on core and Marrow core (api). Pedro is compileOnly, the robot project supplies it.

| Class | Purpose |
| --- | --- |
| `WeaverRoute` | `Route<PathResult>` backed by Marrow's Weaver. `fromFetchPose`, `fromPose`, `avoid(Zone)`, `maxTargets`, `ordered`, `endAt` |
| `PedroBridge` | `poseOf(follower)`, `follow(follower, result)`, `drive(follower)` |
| `BallChaser` | Chase loop: `update()`, `next()`, `state()`, `target()`. Settings: `stopDistanceIn`, `arriveToleranceIn`, `replanDistanceIn`, `minReplanMs`, `lostTimeoutMs`, `avoid(zone)` |
| `ChaseDrive` | Four-method interface BallChaser drives through (`pose`, `follow`, `busy`, `hold`). `PedroBridge.drive` implements it for Pedro 3 |

Zones, `Point`, `PathResult` and `PedroPathingConverter` are Marrow's own. Robot size and clearance are set with `Weaver.setConfig(new PathConfig()...)`.

## Model

Hive Vision v2 SSD-MobileNetV2, classes in order yellow_pollen, red_nectar, blue_nectar. Upload `labels.txt` with the model so class names arrive as text. A model that reports a different label shows up in `unknownLabelCount()`.
