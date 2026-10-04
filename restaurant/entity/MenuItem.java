package restaurant.entity;

// Base class for menu items (abstract class requirement)
public abstract class MenuItem {

    protected int itemId;
    protected String name;
    protected double price;
    protected String description;
    protected int basePrepMinutes;

    public MenuItem(int itemId, String name, double price, String description, int basePrepMinutes) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.basePrepMinutes = basePrepMinutes;
    }

    // HotDish and ColdDish calculate this differently
    public abstract int getEffectivePrepMinutes();

    public int getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getBasePrepMinutes() {
        return basePrepMinutes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBasePrepMinutes(int basePrepMinutes) {
        this.basePrepMinutes = basePrepMinutes;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
