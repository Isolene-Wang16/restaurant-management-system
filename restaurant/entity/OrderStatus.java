package restaurant.entity;

// The three steps an order goes through
public enum OrderStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    // Text shown in the GUI table
    public String getDisplayName() {
        return displayName;
    }

    // Convert button text or file text to enum
    public static OrderStatus fromString(String value) {
        if (value == null || value.trim().length() == 0) {
            throw new IllegalArgumentException("Status must not be empty.");
        }
        String s = value.trim().toUpperCase().replace(' ', '_');
        if (s.equals("PROGRESS")) {
            s = "IN_PROGRESS";
        }
        return OrderStatus.valueOf(s);
    }
}
