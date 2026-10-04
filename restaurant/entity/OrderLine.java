package restaurant.entity;

// One row in an order: a dish and how many
public class OrderLine {

    private MenuItem menuItem;
    private int quantity;

    public OrderLine(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item must not be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        if (quantity == 1) {
            return menuItem.getName();
        }
        return menuItem.getName() + " x" + quantity;
    }
}
