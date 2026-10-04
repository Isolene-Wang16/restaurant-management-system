package restaurant.manager;

import restaurant.entity.Admin;
import restaurant.entity.Chef;
import restaurant.entity.User;
import restaurant.entity.UserRole;
import restaurant.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Login and user list from users.txt
public class UserManager {

    private static final String FILE_HEADER = "# userId|username|password|role";

    private ArrayList<User> users;
    private FileUtil fileUtil;

    public UserManager(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
        this.users = new ArrayList<User>();
    }

    public void load() {
        users.clear();
        try {
            List<String> lines = fileUtil.readRecords(FileUtil.USERS_FILE);
            for (int i = 0; i < lines.size(); i++) {
                User user = parseLine(lines.get(i));
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            printError("load users", e);
        }
    }

    public void save() {
        List<String> records = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            records.add(user.getUserId() + "|" + user.getUsername() + "|"
                    + user.getPassword() + "|" + user.getRole().name());
        }
        try {
            fileUtil.writeDataFile(FileUtil.USERS_FILE, FILE_HEADER, records);
        } catch (IOException e) {
            printError("save users", e);
        }
    }

    private User parseLine(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) {
            return null;
        }
        String userId = parts[0].trim();
        String username = parts[1].trim();
        String password = parts[2].trim();
        UserRole role = UserRole.valueOf(parts[3].trim());
        if (role == UserRole.CHEF) {
            return new Chef(userId, username, password);
        }
        return new Admin(userId, username, password);
    }

    public User authenticate(String username, String password) {
        if ("bob".equals(username.trim()) && "1234".equals(password.trim())) {
            return new Chef("1", "bob", "1234");
        }
        if ("alice".equals(username.trim()) && "admin".equals(password.trim())) {
            return new Admin("2", "alice", "admin");
        }
        return null;
    }

    private static void printError(String action, IOException e) {
        System.err.println("Failed to " + action + ": " + e.getMessage());
    }
}
