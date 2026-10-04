package restaurant.manager;

import restaurant.entity.Order;
import restaurant.entity.OrderStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Shows warning when cooking takes too long
public class TimeoutChecker {

    private OrderManager orderManager;

    public TimeoutChecker(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    public List<Order> getOverdueOrders() {
        List<Order> overdue = new ArrayList<Order>();
        LocalDateTime now = LocalDateTime.now();
        List<Order> all = orderManager.getAllOrders();
        for (int i = 0; i < all.size(); i++) {
            Order order = all.get(i);
            if (order.getStatus() != OrderStatus.IN_PROGRESS) {
                continue;
            }
            if (order.getStartedAt() == null) {
                continue;
            }
            long minutes = ChronoUnit.MINUTES.between(order.getStartedAt(), now);
            if (minutes > order.getMaxPrepMinutes()) {
                overdue.add(order);
            }
        }
        return overdue;
    }

    public String buildAlertMessage() {
        return formatAlerts(getOverdueOrders());
    }

    public String formatAlerts(List<Order> overdue) {
        if (overdue.isEmpty()) {
            return "No overdue orders.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < overdue.size(); i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append("Order ");
            sb.append(overdue.get(i).getOrderId());
            sb.append(" is overdue!");
        }
        return sb.toString();
    }
}
