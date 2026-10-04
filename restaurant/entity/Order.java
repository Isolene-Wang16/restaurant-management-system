package restaurant.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// One order in the kitchen
public class Order {

    private int orderId;
    private LocalDateTime orderTime;
    private OrderStatus status;
    private LocalDateTime startedAt;
    private ArrayList<OrderLine> lines;

    public Order(int orderId, LocalDateTime orderTime, OrderStatus status) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.status = status;
        this.lines = new ArrayList<OrderLine>();
    }

    public void addLine(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("Order line must not be null.");
        }
        lines.add(line);
    }

    // Add up price of all dishes
    public double getTotalPrice() {
        double total = 0.0;
        for (int i = 0; i < lines.size(); i++) {
            total += lines.get(i).getSubtotal();
        }
        return total;
    }

    // Longest prep time in this order (for timeout alert)
    public int getMaxPrepMinutes() {
        int max = 0;
        for (int i = 0; i < lines.size(); i++) {
            int prep = lines.get(i).getMenuItem().getEffectivePrepMinutes();
            if (prep > max) {
                max = prep;
            }
        }
        return max;
    }

    // Status can only move forward one step
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == null) {
            return false;
        }
        if (status == OrderStatus.PENDING && newStatus == OrderStatus.IN_PROGRESS) {
            return true;
        }
        if (status == OrderStatus.IN_PROGRESS && newStatus == OrderStatus.COMPLETED) {
            return true;
        }
        return false;
    }

    public void transitionTo(OrderStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException("Bad status change for order " + orderId);
        }
        // Remember when cooking started
        if (newStatus == OrderStatus.IN_PROGRESS && startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        status = newStatus;
    }

    // Show dish names in the table, like "Rice + Soup"
    public String getDishesSummary() {
        if (lines.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(lines.get(i).toString());
        }
        return sb.toString();
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public List<OrderLine> getLines() {
        return new ArrayList<OrderLine>(lines);
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}
