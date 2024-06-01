package com.example.peltondata;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private boolean isAdmin;
    private String password;

    public User(String username, String firstName, String lastName, String phoneNumber, String emailAddress, boolean isAdmin, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.isAdmin = isAdmin;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getPassword() {
        return password;
    }
}
