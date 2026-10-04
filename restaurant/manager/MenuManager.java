package restaurant.manager;

import restaurant.entity.ColdDish;
import restaurant.entity.HotDish;
import restaurant.entity.MenuItem;
import restaurant.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Menu CRUD and save to menu_items.txt
public class MenuManager {

    private static final String FILE_HEADER = "# id|type|name|price|description|basePrepMinutes";

    private ArrayList<MenuItem> items;
    private FileUtil fileUtil;
    private int nextItemId;

    public MenuManager(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
        this.items = new ArrayList<MenuItem>();
        this.nextItemId = 1;
    }

    public void load() {
        items.clear();
        nextItemId = 1;
        try {
            List<String> lines = fileUtil.readRecords(FileUtil.MENU_FILE);
            for (int i = 0; i < lines.size(); i++) {
                MenuItem item = parseLine(lines.get(i));
                if (item != null) {
                    items.add(item);
                    if (item.getItemId() >= nextItemId) {
                        nextItemId = item.getItemId() + 1;
                    }
                }
            }
        } catch (IOException e) {
            printError("load menu", e);
        }
    }

    public void save() {
        List<String> records = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            String type = "HOT";
            if (item instanceof ColdDish) {
                type = "COLD";
            }
            records.add(item.getItemId() + "|" + type + "|" + item.getName() + "|"
                    + item.getPrice() + "|" + item.getDescription() + "|"
                    + item.getBasePrepMinutes());
        }
        try {
            fileUtil.writeDataFile(FileUtil.MENU_FILE, FILE_HEADER, records);
        } catch (IOException e) {
            printError("save menu", e);
        }
    }

    private MenuItem parseLine(String line) {
        String[] parts = line.split("\\|", 6);
        if (parts.length < 6) {
            return null;
        }
        return makeItem(
                Integer.parseInt(parts[0].trim()),
                parts[1].trim(),
                parts[2].trim(),
                Double.parseDouble(parts[3].trim()),
                parts[4].trim(),
                Integer.parseInt(parts[5].trim()));
    }

    // Factory method - returns HotDish or ColdDish
    public static MenuItem makeItem(int id, String type, String name,
                                    double price, String desc, int prepMin) {
        if (type.equalsIgnoreCase("COLD")) {
            return new ColdDish(id, name, price, desc, prepMin);
        }
        return new HotDish(id, name, price, desc, prepMin);
    }

    public MenuItem getItemById(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId() == id) {
                return items.get(i);
            }
        }
        return null;
    }

    public List<MenuItem> getAllItems() {
        if (items.isEmpty()) {
            // Hot Dish
            items.add(new HotDish(1, "Kung Pao Chicken", 28.5, "Spicy Sichuan dish", 15));
            items.add(new HotDish(2, "Fried Rice", 12.0, "Classic fried rice", 10));
            items.add(new HotDish(3, "Tomato Soup", 12.0, "Warm soup", 8));
            // Cold Dish
            items.add(new ColdDish(4, "Cucumber Salad", 8.0, "Fresh and light", 5));
            nextItemId = 5;
        }
        return new ArrayList<>(items);
    }

    public void addItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Menu item must not be null.");
        }
        items.add(item);
        if (item.getItemId() >= nextItemId) {
            nextItemId = item.getItemId() + 1;
        }
        save();
    }

    // Overloaded add - method overloading requirement
    public MenuItem addItem(String type, String name, double price, String desc, int prepMin) {
        MenuItem item = makeItem(nextItemId, type, name, price, desc, prepMin);
        nextItemId++;
        addItem(item);
        return item;
    }

    public void updateItem(int id, String name, double price, String desc, int prepMin) {
        MenuItem item = getItemById(id);
        if (item == null) {
            throw new IllegalArgumentException("Menu item not found: " + id);
        }
        item.setName(name);
        item.setPrice(price);
        item.setDescription(desc);
        item.setBasePrepMinutes(prepMin);
        save();
    }

    public void deleteItem(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId() == id) {
                items.remove(i);
                break;
            }
        }
        save();
    }

    public List<MenuItem> searchByName(String keyword) {
    	if (keyword == null || keyword.trim().length() == 0) {
            return getAllItems();
        }
        List<MenuItem> result = new ArrayList<MenuItem>();
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            boolean matchName = FileUtil.containsIgnoreCase(item.getName(), keyword);
            String type = (item instanceof ColdDish) ? "Cold" : "Hot";
            boolean matchType = type.equalsIgnoreCase(keyword.trim());
            
            if (matchName || matchType) {
                result.add(item);
            }
        }
        return result;
    }

    private static void printError(String action, IOException e) {
        System.err.println("Failed to " + action + ": " + e.getMessage());
    }
}
