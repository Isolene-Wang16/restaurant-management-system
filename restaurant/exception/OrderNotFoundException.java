package restaurant.exception;

// Used when order id does not exist
public class OrderNotFoundException extends Exception {

    private int orderId;

    public OrderNotFoundException(int orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
