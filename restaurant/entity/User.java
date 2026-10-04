package restaurant.entity;

import restaurant.gui.Main;

import javax.swing.JFrame;

// Parent class for Chef and Admin (abstract class requirement)
public abstract class User {

    protected String userId;
    protected String username;
    protected UserRole role;
    protected String password;
    protected boolean loggedIn;

    public User(String userId, String username, UserRole role, String password) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.password = password;
        this.loggedIn = false;
    }

    // Check username and password from users.txt
    public boolean login(String inputUsername, String inputPassword) {
        if (inputUsername == null || inputPassword == null) {
            return false;
        }
        if (username.equals(inputUsername) && password.equals(inputPassword)) {
            loggedIn = true;
            onLoginSuccess();
            return true;
        }
        return false;
    }

    protected void onLoginSuccess() {
        System.out.println("[Login] " + role + " logged in: " + username);
    }

    public void logout() {
        loggedIn = false;
    }

    // Chef and Admin show different parts of the main window
    public void viewPanel(JFrame parent) {
        if (parent instanceof Main) {
            Main mainFrame = (Main) parent;
            mainFrame.applyRoleView(isAdmin());
        }
    }

    // Chef returns false, Admin returns true (override in subclass)
    protected abstract boolean isAdmin();

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // Needed when saving users.txt
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
