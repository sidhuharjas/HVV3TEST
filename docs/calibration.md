# Calibration

Field positions are only as good as four measurements. All lengths in inches.

| Setting | How to measure |
| --- | --- |
| `camHeight` | Floor to the center of the lens, robot sitting on the field tiles |
| `camPitchDeg` | Angle of the lens axis below horizontal. Phone level app on the camera body, or measure rise over run along the top of the case |
| `camXOffset` | Lens to robot center, positive toward the front |
| `camYOffset` | Lens to robot center, positive toward the left (negative on the right side) |
| `ballHeight` | Floor to the center of a game element |

Robot center is the point your Pedro pose refers to (usually the center of the drivetrain).

## Checking it

1. Put the robot on a known pose and call `follower.setPose(...)` to match.
2. Place one element at a measured spot, 20 to 40 inches ahead of the lens.
3. Log `detection.getX()` and `detection.getY()` and compare with the tape measure.

| What you see | Likely cause |
| --- | --- |
| Distance is off by a consistent amount, left/right is fine | `camPitchDeg` or `camHeight` |
| Error grows with distance | `camPitchDeg` |
| Left/right is off | `camYOffset`, or the camera isn't pointing straight ahead |
| Everything shifted by the same amount | `camXOffset`, or the pose is wrong |
| Positions move when the robot turns | Pose heading is in degrees; it must be radians |

The projection assumes the camera points straight ahead and is not rolled. Use the Limelight crosshair calibration at zero.

## Staleness and confidence

`maxStalenessMs` drops frames older than the value. `minConfidence` of 0.44 is a starting point; raise it if shadows or off-field objects still show up. Hive Vision v2 reports up to 25 boxes per frame.

## Coordinate frame

Field inches, origin at a corner, x and y positive in Pedro's frame, heading in radians counter-clockwise. The center of the 144 inch field is (72, 72).
