package restaurant.gui;

import restaurant.entity.ColdDish;
import restaurant.entity.MenuItem;
import restaurant.entity.Order;
import restaurant.entity.OrderLine;
import restaurant.entity.User;
import restaurant.exception.InvalidStatusException;
import restaurant.exception.OrderNotFoundException;
import restaurant.manager.AppContext;
import restaurant.manager.OrderManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Main GUI window - login and all screens are in this one frame
public class Main extends JFrame {

    private static final String TITLE = "Restaurant Kitchen Order Management System";
    private static final String CARD_LOGIN = "login";
    private static final String CARD_APP = "app";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private AppContext app;
    private User currentUser;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JLabel loginErrorLabel;

    private JLabel userLabel;
    private JLabel roleLabel;
    private JLabel statusLabel;
    private JLabel timeoutLabel;

    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    private JTextField orderSearchField;
    private JComboBox<String> newOrderDishCombo;
    private JTextField newOrderQtyField;

    private DefaultTableModel menuTableModel;
    private JTable menuTable;
    private JTextField menuNameField;
    private JTextField menuPriceField;
    private JTextField menuDescField;
    private JTextField menuPrepField;
    private JComboBox<String> menuTypeCombo;
    private JTextField menuSearchField;

    private JTextArea statsArea;
    private DefaultTableModel historyTableModel;

    private JButton btnMenuAdd;
    private JButton btnMenuEdit;
    private JButton btnMenuDelete;
    private JButton btnCreateOrder;

    public Main(AppContext app) {
        super(TITLE);
        this.app = app;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // CardLayout switches between login page and main page
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(buildLoginPanel(), CARD_LOGIN);
        cardPanel.add(buildAppPanel(), CARD_APP);
        setContentPane(cardPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                app.saveAll();
            }
        });
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 120, 40, 120));

        JLabel title = new JLabel(TITLE, JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(title);

        loginUserField = new JTextField();
        loginPassField = new JPasswordField();
        panel.add(labeledField("Username:", loginUserField));
        panel.add(labeledField("Password:", loginPassField));

        loginErrorLabel = new JLabel(" ", JLabel.CENTER);
        loginErrorLabel.setForeground(Color.RED);
        panel.add(loginErrorLabel);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });
        panel.add(loginBtn);
        panel.add(new JLabel("Chef: bob / 1234   |   Admin: alice / admin", JLabel.CENTER));
        return panel;
    }

    private JPanel buildAppPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildOrderPanel(), BorderLayout.CENTER);
        root.add(buildBottomSection(), BorderLayout.SOUTH);

        timeoutLabel = new JLabel(" ");
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(new JLabel("Timeout Alert: "), BorderLayout.WEST);
        footer.add(timeoutLabel, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(root, BorderLayout.CENTER);
        wrapper.add(footer, BorderLayout.SOUTH);

        // Check overdue orders every 30 seconds
        Timer timer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTimeoutAlert();
            }
        });
        timer.start();
        return wrapper;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel(TITLE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title, BorderLayout.NORTH);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userLabel = new JLabel("User: -");
        roleLabel = new JLabel("Role: -");
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        bar.add(userLabel);
        bar.add(roleLabel);
        bar.add(logoutBtn);
        bar.add(statusLabel);
        top.add(bar, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildOrderPanel() {
        orderTableModel = readOnlyModel("ID", "Dishes", "Status", "Time", "Total");
        orderTable = new JTable(orderTableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        orderSearchField = new JTextField(12);
        newOrderDishCombo = new JComboBox<String>();
        newOrderQtyField = new JTextField("1", 4);

        btnCreateOrder = new JButton("Create Order");
        btnCreateOrder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewOrder();
            }
        });

        final JComboBox<String> sortCombo = new JComboBox<String>(new String[]{
                "Sort: Time ↑", "Sort: Time ↓", "Sort: Status"
        });
        sortCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySort((String) sortCombo.getSelectedItem());
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addOrderButtons(toolbar);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshOrderTable();
            }
        });
        toolbar.add(btnRefresh);

        toolbar.add(new JLabel("Search:"));
        toolbar.add(orderSearchField);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillOrderTable(app.orderManager.searchByDishName(orderSearchField.getText()));
            }
        });
        toolbar.add(btnSearch);
        toolbar.add(sortCombo);
        toolbar.add(new JLabel("New:"));
        toolbar.add(newOrderDishCombo);
        toolbar.add(newOrderQtyField);
        toolbar.add(btnCreateOrder);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Orders"));
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    // Add the three status buttons for orders
    private void addOrderButtons(JPanel toolbar) {
        JButton btnPending = new JButton("Pending");
        btnPending.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedOrderStatus("PENDING");
            }
        });
        JButton btnProgress = new JButton("In Progress");
        btnProgress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedOrderStatus("IN_PROGRESS");
            }
        });
        JButton btnDone = new JButton("Completed");
        btnDone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedOrderStatus("COMPLETED");
            }
        });
        toolbar.add(btnPending);
        toolbar.add(btnProgress);
        toolbar.add(btnDone);
    }

    private JPanel buildBottomSection() {
        JPanel bottom = new JPanel(new GridLayout(1, 2, 8, 8));
        bottom.add(buildMenuPanel());
        bottom.add(buildStatsPanel());
        return bottom;
    }

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Menu Management"));

        menuTableModel = readOnlyModel("ID", "Name", "Type", "Price", "Prep(min)");
        menuTable = new JTable(menuTableModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = menuTable.getSelectedRow();
                if (selectedRow != -1) {
                
                    String id = menuTableModel.getValueAt(selectedRow, 0).toString();
                    String name = menuTableModel.getValueAt(selectedRow, 1).toString();
                    String type = menuTableModel.getValueAt(selectedRow, 2).toString();
                    String price = menuTableModel.getValueAt(selectedRow, 3).toString();
                    String prep = menuTableModel.getValueAt(selectedRow, 4).toString();

                    menuNameField.setText(name);
                    menuPriceField.setText(price);
                    menuPrepField.setText(prep);
                    menuTypeCombo.setSelectedItem(type);

                    MenuItem selectedItem = app.menuManager.getItemById(Integer.parseInt(id));
                    if (selectedItem != null) {
                        menuDescField.setText(selectedItem.getDescription());
                    }
                } else {
                    clearMenuForm();
                }
            }
        });
        
        menuNameField = new JTextField();
        menuPriceField = new JTextField();
        menuDescField = new JTextField();
        menuPrepField = new JTextField();
        menuTypeCombo = new JComboBox<String>(new String[]{"HOT", "COLD"});
        menuSearchField = new JTextField(10);

        JPanel form = new JPanel(new GridLayout(5, 2, 4, 4));
        addFormRow(form, "Name:", menuNameField);
        addFormRow(form, "Price:", menuPriceField);
        addFormRow(form, "Description:", menuDescField);
        addFormRow(form, "Prep (min):", menuPrepField);
        addFormRow(form, "Type:", menuTypeCombo);

        btnMenuAdd = new JButton("Add");
        btnMenuAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMenuItem();
            }
        });
        btnMenuEdit = new JButton("Edit");
        btnMenuEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMenuItem();
            }
        });
        btnMenuDelete = new JButton("Delete");
        btnMenuDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMenuItem();
            }
        });

        JPanel menuBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuBtnRow.add(btnMenuAdd);
        menuBtnRow.add(btnMenuEdit);
        menuBtnRow.add(btnMenuDelete);
        menuBtnRow.add(new JLabel("Search:"));
        menuBtnRow.add(menuSearchField);
        JButton menuSearchBtn = new JButton("Search Menu");
        menuSearchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillMenuTable(app.menuManager.searchByName(menuSearchField.getText()));
            }
        });
        menuBtnRow.add(menuSearchBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(menuBtnRow, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Statistics / Report & Order History"));

        statsArea = new JTextArea(6, 30);
        statsArea.setEditable(false);

        JPanel reportBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dailyBtn = new JButton("Today Revenue");
        dailyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDailyRevenue();
            }
        });
        JButton monthBtn = new JButton("Monthly Sales");
        monthBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMonthlySales();
            }
        });
        JButton rankBtn = new JButton("Best-selling Dishes");
        rankBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRanking();
            }
        });
        reportBtns.add(dailyBtn);
        reportBtns.add(monthBtn);
        reportBtns.add(rankBtn);

        historyTableModel = readOnlyModel("ID", "Dishes", "Time", "Total");
        JTable historyTable = new JTable(historyTableModel);

        JPanel top = new JPanel(new BorderLayout());
        top.add(reportBtns, BorderLayout.NORTH);
        top.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        JPanel histWrap = new JPanel(new BorderLayout());
        histWrap.setBorder(BorderFactory.createTitledBorder("Order History (Completed)"));
        histWrap.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(histWrap, BorderLayout.CENTER);
        return panel;
    }

    private void attemptLogin() {
        User user = app.userManager.authenticate(
                loginUserField.getText().trim(),
                new String(loginPassField.getPassword()));
        if (user == null) {
            loginErrorLabel.setText("Invalid username or password.");
            return;
        }
        loginErrorLabel.setText(" ");
        loginPassField.setText("");
        setCurrentUser(user);
        cardLayout.show(cardPanel, CARD_APP);
    }

    // Called from User.viewPanel - chef vs admin
    public void applyRoleView(boolean admin) {
        setAdminControlsEnabled(admin);
        refreshAllViews();
        if (admin) {
            showDailyRevenue();
        }
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        userLabel.setText("User: " + user.getUsername());
        roleLabel.setText("Role: " + user.getRole());
        user.viewPanel(this);
    }

    private void refreshAllViews() {
        refreshOrderTable();
        refreshNewOrderCombo();
        fillMenuTable(app.menuManager.getAllItems());
        refreshHistoryTable();
        refreshTimeoutAlert();
        showStatus(" ");
    }

    private void refreshNewOrderCombo() {
        newOrderDishCombo.removeAllItems();
        List<MenuItem> items = app.menuManager.getAllItems();
        for (int i = 0; i < items.size(); i++) {
            newOrderDishCombo.addItem(items.get(i).getName());
        }
    }

    private void refreshOrderTable() {
        fillOrderTable(app.orderManager.getActiveOrders());
    }

    private void fillOrderTable(List<Order> orders) {
        orderTableModel.setRowCount(0);
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            orderTableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getDishesSummary(),
                    order.getStatus().getDisplayName(),
                    order.getOrderTime().format(TIME_FMT),
                    String.format("%.2f", order.getTotalPrice())
            });
        }
    }

    private void refreshHistoryTable() {
        historyTableModel.setRowCount(0);
        List<Order> done = app.orderManager.getCompletedOrders();
        for (int i = 0; i < done.size(); i++) {
            Order order = done.get(i);
            historyTableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getDishesSummary(),
                    order.getOrderTime().format(TIME_FMT),
                    String.format("%.2f", order.getTotalPrice())
            });
        }
    }

    private void fillMenuTable(List<MenuItem> items) {
        menuTableModel.setRowCount(0);
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            String type = "Hot";
            if (item instanceof ColdDish) {
                type = "Cold";
            }
            menuTableModel.addRow(new Object[]{
                    item.getItemId(), item.getName(), type,
                    String.format("%.2f", item.getPrice()), item.getBasePrepMinutes()
            });
        }
    }

    private void refreshTimeoutAlert() {
        List<Order> overdue = app.timeoutChecker.getOverdueOrders();
        timeoutLabel.setText(app.timeoutChecker.formatAlerts(overdue));
        if (overdue.isEmpty()) {
            timeoutLabel.setForeground(Color.BLACK);
        } else {
            timeoutLabel.setForeground(Color.RED);
        }
    }

    private void applySort(String selection) {
        OrderManager om = app.orderManager;
        if (selection.contains("Time ↑")) {
            om.sortByTime(true);
        } else if (selection.contains("Time ↓")) {
            om.sortByTime(false);
        } else {
            om.sortByStatus();
        }
        refreshOrderTable();
    }

    private Integer getSelectedOrderId() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) orderTableModel.getValueAt(row, 0);
    }

    private void updateSelectedOrderStatus(String status) {
        Integer id = getSelectedOrderId();
        if (id == null) {
            showStatus("Please select an order first.");
            return;
        }
        try {
            app.orderManager.updateOrderStatus(id, status);
            refreshOrderTable();
            refreshHistoryTable();
            refreshTimeoutAlert();
            showStatus(" ");
        } catch (OrderNotFoundException e) {
            showStatus(e.getMessage());
        } catch (InvalidStatusException e) {
            showStatus(e.getMessage());
        }
    }

    private void createNewOrder() {
        if (newOrderDishCombo.getItemCount() == 0) {
            showStatus("No menu items available.");
            return;
        }
        String picked = (String) newOrderDishCombo.getSelectedItem();
        MenuItem item = findMenuItemByName(picked);
        if (item == null) {
            showStatus("Select a valid dish.");
            return;
        }
        try {
            int qty = Integer.parseInt(newOrderQtyField.getText().trim());
            if (qty <= 0) {
                throw new NumberFormatException();
            }
            ArrayList<OrderLine> lines = new ArrayList<OrderLine>();
            lines.add(new OrderLine(item, qty));
            app.orderManager.addOrder(lines);
            refreshOrderTable();
            showStatus("Order created.");
        } catch (NumberFormatException ex) {
            showStatus("Invalid quantity.");
        }
    }

    private MenuItem findMenuItemByName(String name) {
        List<MenuItem> items = app.menuManager.getAllItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getName().equals(name)) {
                return items.get(i);
            }
        }
        return null;
    }

    private void addMenuItem() {
        try {
            app.menuManager.addItem(readMenuType(), readMenuName(), readMenuPrice(),
                    menuDescField.getText().trim(), readMenuPrep());
            fillMenuTable(app.menuManager.getAllItems());
            refreshNewOrderCombo();
            clearMenuForm();
            showStatus("Menu item added.");
        } catch (Exception ex) {
            showStatus(ex.getMessage());
        }
    }

    private void updateMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            showStatus("Select a menu item to edit.");
            return;
        }
        try {
            int id = (Integer) menuTableModel.getValueAt(row, 0);
            app.menuManager.updateItem(id, readMenuName(), readMenuPrice(),
                    menuDescField.getText().trim(), readMenuPrep());
            fillMenuTable(app.menuManager.getAllItems());
            refreshNewOrderCombo();
            showStatus("Menu item updated.");
        } catch (Exception ex) {
            showStatus(ex.getMessage());
        }
    }

    private void deleteMenuItem() {
        int row = menuTable.getSelectedRow();
        if (row < 0) {
            showStatus("Select a menu item to delete.");
            return;
        }
        int id = (Integer) menuTableModel.getValueAt(row, 0);
        app.menuManager.deleteItem(id);
        fillMenuTable(app.menuManager.getAllItems());
        refreshNewOrderCombo();
        clearMenuForm();
        showStatus("Menu item #" + id + " deleted.");
    }

    private String readMenuName() {
        String name = menuNameField.getText().trim();
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name is required.");
        }
        return name;
    }

    private double readMenuPrice() {
        return Double.parseDouble(menuPriceField.getText().trim());
    }

    private int readMenuPrep() {
        return Integer.parseInt(menuPrepField.getText().trim());
    }

    private String readMenuType() {
        return (String) menuTypeCombo.getSelectedItem();
    }

    private void clearMenuForm() {
        menuNameField.setText("");
        menuPriceField.setText("");
        menuDescField.setText("");
        menuPrepField.setText("");
    }

    private void showDailyRevenue() {
        LocalDate today = LocalDate.now();
        statsArea.setText("Today Revenue (" + today + "): $"
                + String.format("%.2f", app.reportGenerator.getDailyRevenue(today))
                + "\nActive orders: " + app.orderManager.getActiveOrders().size());
    }

    private void showMonthlySales() {
        statsArea.setText(app.reportGenerator.formatMonthlyReport(YearMonth.now()));
    }

    private void showRanking() {
        List<String> ranks = app.reportGenerator.getBestSellingDishes(5);
        StringBuilder sb = new StringBuilder("Best-selling Dishes:\n");
        if (ranks.isEmpty()) {
            sb.append("  (no completed orders yet)");
        } else {
            for (int i = 0; i < ranks.size(); i++) {
                sb.append(ranks.get(i)).append('\n');
            }
        }
        statsArea.setText(sb.toString());
    }

    // Chef cannot edit menu or create orders
    private void setAdminControlsEnabled(boolean enabled) {
        btnMenuAdd.setEnabled(enabled);
        btnMenuEdit.setEnabled(enabled);
        btnMenuDelete.setEnabled(enabled);
        menuNameField.setEnabled(enabled);
        menuPriceField.setEnabled(enabled);
        menuDescField.setEnabled(enabled);
        menuPrepField.setEnabled(enabled);
        menuTypeCombo.setEnabled(enabled);
        menuSearchField.setEnabled(enabled);
        newOrderDishCombo.setEnabled(enabled);
        newOrderQtyField.setEnabled(enabled);
        btnCreateOrder.setEnabled(enabled);
    }

    private void logout() {
        if (currentUser != null) {
            currentUser.logout();
        }
        app.saveAll();
        loginUserField.setText("");
        loginPassField.setText("");
        loginErrorLabel.setText(" ");
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    private void showStatus(String message) {
        if (message == null || message.trim().length() == 0) {
            statusLabel.setText(" ");
        } else {
            statusLabel.setText(message);
        }
    }

    private static JPanel labeledField(String label, Component field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static DefaultTableModel readOnlyModel(String c1, String c2, String c3, String c4, String c5) {
        return readOnlyModel(new String[]{c1, c2, c3, c4, c5});
    }

    private static DefaultTableModel readOnlyModel(String c1, String c2, String c3, String c4) {
        return readOnlyModel(new String[]{c1, c2, c3, c4});
    }

    private static void addFormRow(JPanel form, String label, Component field) {
        form.add(new JLabel(label));
        form.add(field);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main main = new Main(AppContext.create());
                main.setVisible(true);
            }
        });
    }
}
