package restaurant.entity;

// Hot food uses the full prep time from the menu
public class HotDish extends MenuItem {

    public HotDish(int itemId, String name, double price, String description, int basePrepMinutes) {
        super(itemId, name, price, description, basePrepMinutes);
    }

    @Override
    public int getEffectivePrepMinutes() {
        return basePrepMinutes;
    }
}
