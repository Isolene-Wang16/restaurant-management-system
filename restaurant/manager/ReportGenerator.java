package restaurant.manager;

import restaurant.entity.Order;
import restaurant.entity.OrderLine;
import restaurant.entity.OrderStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Sales numbers for the admin report buttons
public class ReportGenerator {

    private OrderManager orderManager;

    public ReportGenerator(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    public double getDailyRevenue(LocalDate date) {
        double total = 0.0;
        List<Order> all = orderManager.getAllOrders();
        for (int i = 0; i < all.size(); i++) {
            Order order = all.get(i);
            if (order.getStatus() == OrderStatus.COMPLETED
                    && order.getOrderTime().toLocalDate().equals(date)) {
                total += order.getTotalPrice();
            }
        }
        return total;
    }

    public Map<String, Double> getMonthlySalesByItem(YearMonth month) {
        Map<String, Double> sales = new HashMap<String, Double>();
        List<Order> all = orderManager.getAllOrders();
        for (int i = 0; i < all.size(); i++) {
            Order order = all.get(i);
            if (order.getStatus() != OrderStatus.COMPLETED) {
                continue;
            }
            if (!YearMonth.from(order.getOrderTime()).equals(month)) {
                continue;
            }
            List<OrderLine> lines = order.getLines();
            for (int j = 0; j < lines.size(); j++) {
                OrderLine line = lines.get(j);
                String name = line.getMenuItem().getName();
                double old = 0.0;
                if (sales.containsKey(name)) {
                    old = sales.get(name);
                }
                sales.put(name, old + line.getSubtotal());
            }
        }
        return sales;
    }

    public List<String> getBestSellingDishes(int topN) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        List<Order> done = orderManager.getCompletedOrders();
        for (int i = 0; i < done.size(); i++) {
            List<OrderLine> lines = done.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                OrderLine line = lines.get(j);
                String name = line.getMenuItem().getName();
                int old = 0;
                if (counts.containsKey(name)) {
                    old = counts.get(name);
                }
                counts.put(name, old + line.getQuantity());
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(counts.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        List<String> result = new ArrayList<String>();
        int limit = topN;
        if (list.size() < limit) {
            limit = list.size();
        }
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            result.add((i + 1) + ". " + entry.getKey() + " - sold " + entry.getValue());
        }
        return result;
    }

    // Build the text for monthly sales button
    public String formatMonthlyReport(YearMonth month) {
        Map<String, Double> sales = getMonthlySalesByItem(month);
        if (sales.isEmpty()) {
            return "No completed sales for " + month + ".";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly sales (").append(month).append("):\n");

        List<String> names = new ArrayList<String>(sales.keySet());
        while (names.size() > 0) {
            String bestName = names.get(0);
            double bestVal = sales.get(bestName);
            int bestIndex = 0;
            for (int i = 1; i < names.size(); i++) {
                String n = names.get(i);
                if (sales.get(n) > bestVal) {
                    bestVal = sales.get(n);
                    bestName = n;
                    bestIndex = i;
                }
            }
            sb.append("  ").append(bestName).append(": $")
                    .append(String.format("%.2f", bestVal)).append("\n");
            names.remove(bestIndex);
        }
        return sb.toString();
    }
}
