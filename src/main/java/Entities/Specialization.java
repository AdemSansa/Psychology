package Entities;

public enum Specialization {
    ADDICTIONS("Addictologie"),
    DEPRESSION("Dépression & Anxiété"),
    COUPLE("Thérapie de Couple"),
    CHILD("Pédopsychologie"),
    SEXOLOGY("Sexologie"),
    NEURO("Neuropsychologie"),
    GENERAL("Psychologie Générale");

    private final String displayName;

    Specialization(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Specialization fromDisplayName(String text) {
        for (Specialization b : Specialization.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
