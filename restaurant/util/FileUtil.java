package restaurant.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

    public static final String DATA_DIR = "data";
    public static final String MENU_FILE = DATA_DIR + File.separator + "menu_items.txt";
    public static final String ORDERS_FILE = DATA_DIR + File.separator + "orders.txt";
    public static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";

    private static final String MENU_HEADER = "# id|type|name|price|description|basePrepMinutes";
    private static final String ORDERS_HEADER = "# orderId|status|orderTime|startedAt|itemId:qty,...";
    private static final String USERS_HEADER = "# userId|username|password|role";

    public FileUtil() {
        makeDataFolder();
    }

    public void makeDataFolder() {
        Path dir = Paths.get(DATA_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                System.err.println("Could not create data folder: " + e.getMessage());
            }
        }
    }

    public void ensureDataDirectory() {
        makeDataFolder();
    }

    public void seedDefaultsIfEmpty() {
        makeDataFolder();
        try {
            seedOneFile(MENU_FILE, MENU_HEADER, sampleMenu());
            seedOneFile(USERS_FILE, USERS_HEADER, sampleUsers());
            seedOneFile(ORDERS_FILE, ORDERS_HEADER, sampleOrders());
        } catch (IOException e) {
            System.err.println("Could not create sample files: " + e.getMessage());
        }
    }

    private void seedOneFile(String path, String header, List<String> records) throws IOException {
        if (!fileExists(path) || readRecords(path).isEmpty()) {
            writeDataFile(path, header, records);
        }
    }

    public List<String> readRecords(String filePath) throws IOException {
        List<String> records = new ArrayList<>();
        List<String> all = readAllLines(filePath);
        for (String line : all) {
            if (isDataLine(line)) {
                records.add(line);
            }
        }
        return records;
    }

    public void writeDataFile(String filePath, String header, List<String> records) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(header);
        lines.addAll(records);
        writeAllLines(filePath, lines);
    }

    public List<String> readAllLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public void writeAllLines(String filePath, List<String> lines) throws IOException {
        makeDataFolder();
        Path path = Paths.get(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static boolean isDataLine(String line) {
        return line != null && !line.isBlank() && !line.startsWith("#");
    }

    public static boolean containsIgnoreCase(String text, String keyword) {
        if (keyword == null || keyword.isBlank()) return false;
        return text != null && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private static List<String> sampleMenu() {
        return Arrays.asList(
                "1|HOT|Kung Pao Chicken|28.50|Spicy Sichuan dish|15",
                "2|HOT|Fried Rice|12.00|Classic fried rice|10",
                "3|COLD|Cucumber Salad|8.00|Fresh and light|5",
                "4|HOT|Tomato Soup|12.00|Warm soup|8",
                "5|HOT|Grilled Steak|45.00|Premium beef|20");
    }

    private static List<String> sampleUsers() {
        return Arrays.asList(
                "U001|bob|1234|CHEF",
                "U002|alice|admin|ADMIN");
    }

    private static List<String> sampleOrders() {
        return Arrays.asList(
                "101|PENDING|2026-05-28T14:30:00||1:1,2:1",
                "102|IN_PROGRESS|2026-05-28T14:31:00|2026-05-28T14:32:00|3:1",
                "103|COMPLETED|2026-05-27T12:00:00|2026-05-27T12:05:00|1:2,4:1",
                "104|PENDING|2026-05-28T15:00:00||5:1",
                "105|COMPLETED|2026-05-28T11:00:00|2026-05-28T11:05:00|2:2,3:1");
    }
}