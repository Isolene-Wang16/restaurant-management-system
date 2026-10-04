package restaurant.entity;

// Chef can view orders and change status
public class Chef extends User {

    public Chef(String userId, String username, String password) {
        super(userId, username, UserRole.CHEF, password);
    }

    @Override
    protected boolean isAdmin() {
        return false;
    }
}
