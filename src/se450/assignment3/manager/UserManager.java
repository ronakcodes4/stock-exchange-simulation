package se450.assignment3.manager;

import se450.assignment3.user.User;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class UserManager {
    private static volatile UserManager instance;
    private final TreeMap<String, User> users;

    private UserManager() {
        this.users = new TreeMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager();
                }
            }
        }
        return instance;
    }

    public void init(String[] userIdsIn) throws DataValidationException {
        if (userIdsIn == null) {
            throw new DataValidationException("Input user ID array cannot be null for UserManager.init.");
        }
        for (String userId : userIdsIn) {
            if (userId == null || userId.trim().isEmpty()){
                throw new DataValidationException("User ID in array cannot be null or empty.");
            }
            String upperUserId = userId.toUpperCase();
            if (users.containsKey(upperUserId)) {
                // System.out.println("User " + upperUserId + " already initialized. Skipping."); // Per A3 output, no such message
                continue;
            }
            User newUser = new User(userId);
            users.put(newUser.getUserId(), newUser);
        }
    }

    public void updateTradable(String userId, TradableDTO dto) throws DataValidationException {
        if (userId == null) {
            throw new DataValidationException("User ID cannot be null for updating tradable.");
        }
        if (dto == null) {
            throw new DataValidationException("TradableDTO cannot be null for updating.");
        }
        User user = users.get(userId.toUpperCase());
        if (user == null) {
            throw new DataValidationException("User " + userId.toUpperCase() + " does not exist in UserManager.");
        }
        user.updateTradable(dto);
    }

    @Override
    public String toString() {
        StringBuilder finalSb = new StringBuilder();
        ArrayList<User> userList = new ArrayList<>(users.values()); // Ensures consistent iteration order from TreeMap
        for (int i = 0; i < userList.size(); i++) {
            finalSb.append(userList.get(i).toString()); // User.toString() ends with one \n
            if (i < userList.size() - 1) { // If not the last user, add the extra newline for spacing
                finalSb.append("\n");
            }
        }
        // If the list is empty, an empty string is fine.
        // If the list is not empty, User.toString() provides the final newline for the last user.
        return finalSb.toString();
    }
}
