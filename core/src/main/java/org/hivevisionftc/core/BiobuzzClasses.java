package org.hivevisionftc.core;

/** Classes the Limelight 3A detector reports. Order matches labels.txt in the model folder. */
public enum BiobuzzClasses {
    POLLEN("yellow_pollen"),
    NECTAR_RED("red_nectar"),
    NECTAR_BLUE("blue_nectar");

    private final String label;

    BiobuzzClasses(String label) { this.label = label; }

    public String label() { return label; }

    /** Matches by label first (case-insensitive), then by model class id. Null if unknown. */
    public static BiobuzzClasses from(String label, int classId) {
        if (label != null) {
            for (BiobuzzClasses c : values()) {
                if (c.label.equalsIgnoreCase(label)) return c;
            }
        }
        BiobuzzClasses[] all = values();
        return (label == null && classId >= 0 && classId < all.length) ? all[classId] : null;
    }
}
