package com.lostfound.model;

public class User {
    private Integer userId;
    private String name;
    private String email;
    private String password;
    private String role = "USER";
    private boolean active = true;

    public User() {
    }

    public User(Integer userId, String name, String email, String password) {
        this(userId, name, email, password, "USER", true);
    }

    public User(Integer userId, String name, String email, String password, String role, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        setRole(role);
        this.active = active;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role == null || role.isBlank() ? "USER" : role.trim().toUpperCase();
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActiveLabel() {
        return active ? "Active" : "Disabled";
    }
}
