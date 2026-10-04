package restaurant.exception;

// Used when user tries an illegal status change
public class InvalidStatusException extends Exception {

    private int orderId;
    private String fromStatus;
    private String toStatus;

    public InvalidStatusException(int orderId, String fromStatus, String toStatus) {
        super("Invalid status change for order " + orderId + ": " + fromStatus + " to " + toStatus);
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }
}
