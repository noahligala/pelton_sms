package com.example.peltondata.utils;

import com.example.peltondata.User;

import java.util.HashMap;

public class UserManager {
    private HashMap<String, User> users;

    public UserManager() {
        users = new HashMap<>();

        // Initialize predefined users
        addUser(new User("admin", "Admin", "User", "1234567890", "admin@example.com", true, "adminpass"));
        addUser(new User("staff", "Staff", "User", "0987654321", "staff@example.com", false, "staffpass"));
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean validateCredentials(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }
}
