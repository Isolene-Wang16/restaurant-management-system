package restaurant.entity;

// Cold food is usually faster to prepare
public class ColdDish extends MenuItem {

    public ColdDish(int itemId, String name, double price, String description, int basePrepMinutes) {
        super(itemId, name, price, description, basePrepMinutes);
    }

    @Override
    public int getEffectivePrepMinutes() {
        int time = basePrepMinutes - 2;
        if (time < 1) {
            time = 1;
        }
        return time;
    }
}
