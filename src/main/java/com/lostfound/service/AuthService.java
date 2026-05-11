package com.lostfound.service;

import com.lostfound.dao.UserDAO;
import com.lostfound.model.User;
import com.lostfound.util.HashUtil;
import com.lostfound.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User register(String name, String email, String password, String confirmPassword) throws SQLException {
        if (ValidationUtil.isBlank(name) || ValidationUtil.isBlank(email)
                || ValidationUtil.isBlank(password) || ValidationUtil.isBlank(confirmPassword)) {
            throw new IllegalArgumentException("All registration fields are required.");
        }

        String normalizedName = name.trim();
        String normalizedEmail = email.trim().toLowerCase();

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("Name must be 100 characters or fewer.");
        }

        if (normalizedEmail.length() > 150) {
            throw new IllegalArgumentException("Email must be 150 characters or fewer.");
        }

        if (!ValidationUtil.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        Optional<User> existingUser = userDAO.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPassword(HashUtil.sha256(password));

        boolean created = userDAO.createUser(user);
        if (!created) {
            throw new IllegalStateException("Unable to register the user. Please try again.");
        }

        return userDAO.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalStateException("User was created but could not be loaded."));
    }

    public User login(String email, String password) throws SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        Optional<User> existingUser = userDAO.findByEmail(email.trim());
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = existingUser.get();
        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been disabled. Contact an administrator.");
        }

        String hashedPassword = HashUtil.sha256(password);
        if (!hashedPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }
}
