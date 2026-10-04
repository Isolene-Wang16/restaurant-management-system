package restaurant.manager;

import restaurant.util.FileUtil;

// Creates all managers and loads files at startup
public class AppContext {

    public MenuManager menuManager;
    public OrderManager orderManager;
    public UserManager userManager;
    public ReportGenerator reportGenerator;
    public TimeoutChecker timeoutChecker;

    private AppContext(MenuManager menuManager, OrderManager orderManager,
                       UserManager userManager, ReportGenerator reportGenerator,
                       TimeoutChecker timeoutChecker) {
        this.menuManager = menuManager;
        this.orderManager = orderManager;
        this.userManager = userManager;
        this.reportGenerator = reportGenerator;
        this.timeoutChecker = timeoutChecker;
    }

    // Called from Main.main before showing the window
    public static AppContext create() {
        FileUtil fileUtil = new FileUtil();
        fileUtil.seedDefaultsIfEmpty();

        MenuManager menuManager = new MenuManager(fileUtil);
        UserManager userManager = new UserManager(fileUtil);
        menuManager.load();
        userManager.load();

        // Orders need menu items loaded first
        OrderManager orderManager = new OrderManager(fileUtil, menuManager);
        ReportGenerator reportGenerator = new ReportGenerator(orderManager);
        TimeoutChecker timeoutChecker = new TimeoutChecker(orderManager);

        return new AppContext(menuManager, orderManager, userManager,
                reportGenerator, timeoutChecker);
    }

    // Save all txt files
    public void saveAll() {
        menuManager.save();
        orderManager.save();
        userManager.save();
    }
}
