package se450.assignment3.manager;

import se450.assignment3.user.User;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment4.exceptions.UserNotFoundException;

import java.util.ArrayList;
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
            if (userId == null || userId.trim().isEmpty()) {
                throw new DataValidationException("User ID in array cannot be null or empty.");
            }
            String upperUserId = userId.toUpperCase();
            if (!users.containsKey(upperUserId)) {
                User newUser = new User(userId);
                users.put(newUser.getUserId(), newUser);
            }
        }
    }


    public void updateTradable(String userId, TradableDTO dto) throws DataValidationException {
        if (userId == null) {
            throw new DataValidationException("User ID cannot be null for updating tradable.");
        }
        if (dto == null) {
            throw new DataValidationException("TradableDTO cannot be null for updating.");
        }

        String upperUserId = userId.toUpperCase();
        User user = users.get(upperUserId);
        if (user == null) {
            throw new DataValidationException("User " + upperUserId + " does not exist in UserManager.");
        }
        user.updateTradable(dto);
    }

    public User getUser(String userId) throws UserNotFoundException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new UserNotFoundException("User ID cannot be null or empty.");
        }
        User user = users.get(userId.toUpperCase()); // Assuming users are stored with uppercase IDs
        if (user == null) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        return user;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        ArrayList<User> userList = new ArrayList<>(users.values());

        for (int i = 0; i < userList.size(); i++) {
            sb.append(userList.get(i).toString());
            if (i < userList.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}