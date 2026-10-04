package restaurant.manager;

import restaurant.entity.HotDish;
import restaurant.entity.MenuItem;
import restaurant.entity.Order;
import restaurant.entity.OrderLine;
import restaurant.entity.OrderStatus;
import restaurant.exception.InvalidStatusException;
import restaurant.exception.OrderNotFoundException;
import restaurant.util.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Keeps the order list and writes orders.txt
public class OrderManager implements OrderManageable {

    private static final String FILE_HEADER = "# orderId|status|orderTime|startedAt|itemId:qty,...";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ArrayList<Order> orders;
    private FileUtil fileUtil;
    private MenuManager menuManager;
    private int nextOrderId;

    public OrderManager(FileUtil fileUtil, MenuManager menuManager) {
        this.fileUtil = fileUtil;
        this.menuManager = menuManager;
        this.orders = new ArrayList<Order>();
        this.nextOrderId = 1;
        load();
    }

    @Override
    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null.");
        }
        orders.add(order);
        if (order.getOrderId() >= nextOrderId) {
            nextOrderId = order.getOrderId() + 1;
        }
        save();
    }

    // Overloaded addOrder - builds a new pending order
    public Order addOrder(List<OrderLine> lines) {
        Order order = new Order(nextOrderId, LocalDateTime.now(), OrderStatus.PENDING);
        nextOrderId++;
        for (int i = 0; i < lines.size(); i++) {
            order.addLine(lines.get(i));
        }
        orders.add(order);
        save();
        return order;
    }

    @Override
    public void updateOrderStatus(int orderId, String status)
            throws OrderNotFoundException, InvalidStatusException {
        Order order = getOrderById(orderId);
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.fromString(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException(orderId, order.getStatus().name(), status);
        }
        if (!order.canTransitionTo(newStatus)) {
            throw new InvalidStatusException(orderId, order.getStatus().name(), newStatus.name());
        }
        order.transitionTo(newStatus);
        save();
    }

    @Override
    public Order getOrderById(int orderId) throws OrderNotFoundException {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId() == orderId) {
                return orders.get(i);
            }
        }
        throw new OrderNotFoundException(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        return new ArrayList<Order>(orders);
    }

    public List<Order> getActiveOrders() {
        List<Order> active = new ArrayList<Order>();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getStatus() != OrderStatus.COMPLETED) {
                active.add(orders.get(i));
            }
        }
        return active;
    }

    public List<Order> getCompletedOrders() {
        List<Order> done = new ArrayList<Order>();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getStatus() == OrderStatus.COMPLETED) {
                done.add(orders.get(i));
            }
        }
        return done;
    }

    // Sort using Comparator (from class notes)
    public void sortByTime(boolean ascending) {
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                return a.getOrderTime().compareTo(b.getOrderTime());
            }
        });
        if (!ascending) {
            Collections.reverse(orders);
        }
    }

    public void sortByStatus() {
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                return a.getStatus().name().compareTo(b.getStatus().name());
            }
        });
    }

    public List<Order> searchByDishName(String keyword) {
        if (keyword == null || keyword.trim().length() == 0) {
            return getActiveOrders();
        }
        List<Order> result = new ArrayList<Order>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getStatus() != OrderStatus.COMPLETED
                    && FileUtil.containsIgnoreCase(order.getDishesSummary(), keyword)) {
                result.add(order);
            }
        }
        return result;
    }

    public void load() {
        orders.clear();
        nextOrderId = 1;
        try {
            List<String> lines = fileUtil.readRecords(FileUtil.ORDERS_FILE);
            for (String line : lines) {
                Order order = parseLine(line);
                if (order != null) {
                    orders.add(order);
                    if (order.getOrderId() >= nextOrderId) {
                        nextOrderId = order.getOrderId() + 1;
                    }
                }
            }
        } catch (IOException e) {
            printError("load orders", e);
        }
    }

    public void save() {
        List<String> records = new ArrayList<String>();
        for (int i = 0; i < orders.size(); i++) {
            records.add(toFileLine(orders.get(i)));
        }
        try {
            fileUtil.writeDataFile(FileUtil.ORDERS_FILE, FILE_HEADER, records);
        } catch (IOException e) {
            printError("save orders", e);
        }
    }

    private String toFileLine(Order order) {
        StringBuilder itemPart = new StringBuilder();
        List<OrderLine> lines = order.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                itemPart.append(",");
            }
            OrderLine line = lines.get(i);
            itemPart.append(line.getMenuItem().getItemId());
            itemPart.append(":");
            itemPart.append(line.getQuantity());
        }
        String started = "";
        if (order.getStartedAt() != null) {
            started = order.getStartedAt().format(DATE_FMT);
        }
        return order.getOrderId()
                + "|" + order.getStatus().name()
                + "|" + order.getOrderTime().format(DATE_FMT)
                + "|" + started
                + "|" + itemPart;
    }

    private Order parseLine(String line) {
        String[] parts = line.split("\\|", 5);
        if (parts.length < 5) {
            return null;
        }
        int orderId = Integer.parseInt(parts[0].trim());
        OrderStatus status = OrderStatus.valueOf(parts[1].trim());
        LocalDateTime orderTime = LocalDateTime.parse(parts[2].trim(), DATE_FMT);
        Order order = new Order(orderId, orderTime, status);

        String started = parts[3].trim();
        if (started.length() > 0) {
            order.setStartedAt(LocalDateTime.parse(started, DATE_FMT));
        }

        String itemsPart = parts[4].trim();
        if (itemsPart.length() > 0) {
            String[] tokens = itemsPart.split(",");
            for (int t = 0; t < tokens.length; t++) {
                String[] pair = tokens[t].split(":");
                if (pair.length == 2) {
                    int itemId = Integer.parseInt(pair[0].trim());
                    int qty = Integer.parseInt(pair[1].trim());
                    MenuItem menuItem = menuManager.getItemById(itemId);
                    if (menuItem == null) {
                        menuItem = new HotDish(itemId, "Item#" + itemId, 0.0, "", 10);
                    }
                    order.addLine(new OrderLine(menuItem, qty));
                }
            }
        }
        return order;
    }

    private static void printError(String action, IOException e) {
        System.err.println("Failed to " + action + ": " + e.getMessage());
    }
}