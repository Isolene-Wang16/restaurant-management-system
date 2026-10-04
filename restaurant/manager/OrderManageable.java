package restaurant.manager;

import restaurant.entity.Order;
import restaurant.exception.InvalidStatusException;
import restaurant.exception.OrderNotFoundException;

import java.util.List;

// Interface required by the project - order operations
public interface OrderManageable {

    void addOrder(Order order);

    void updateOrderStatus(int orderId, String status)
            throws OrderNotFoundException, InvalidStatusException;

    Order getOrderById(int orderId) throws OrderNotFoundException;

    List<Order> getAllOrders();
}
