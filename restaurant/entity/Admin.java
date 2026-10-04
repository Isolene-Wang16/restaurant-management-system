package restaurant.entity;

// Admin can edit menu and view sales reports
public class Admin extends User {

    public Admin(String userId, String username, String password) {
        super(userId, username, UserRole.ADMIN, password);
    }

    @Override
    protected boolean isAdmin() {
        return true;
    }
}
