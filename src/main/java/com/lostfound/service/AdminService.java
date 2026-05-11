package com.lostfound.service;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.UserDAO;
import com.lostfound.model.AdminClaimView;
import com.lostfound.model.AdminItemView;
import com.lostfound.model.Item;
import com.lostfound.model.Claim;
import com.lostfound.model.User;
import com.lostfound.util.DBConnection;
import com.lostfound.util.ImageUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminService {
    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final ClaimDAO claimDAO = new ClaimDAO();

    public List<AdminItemView> getReports(User admin) throws SQLException {
        requireAdmin(admin);
        return itemDAO.findAllForAdmin();
    }

    public List<AdminClaimView> getClaims(User admin) throws SQLException {
        requireAdmin(admin);
        return claimDAO.findAllForAdmin();
    }

    public List<User> getUsers(User admin) throws SQLException {
        requireAdmin(admin);
        return userDAO.findAllUsers();
    }

    public void approveClaim(User admin, int claimId) throws SQLException {
        requireAdmin(admin);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Claim claim = claimDAO.findById(connection, claimId)
                        .orElseThrow(() -> new IllegalArgumentException("Claim not found."));

                if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
                    throw new IllegalArgumentException("Only pending claims can be approved.");
                }

                boolean updated = claimDAO.updateStatus(connection, claimId, "APPROVED");
                boolean itemResolved = itemDAO.updateStatus(connection, claim.getItemId(), "RESOLVED");
                claimDAO.rejectOtherPendingClaims(connection, claim.getItemId(), claimId);

                if (!updated || !itemResolved) {
                    throw new IllegalStateException("Unable to approve the selected claim.");
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void rejectClaim(User admin, int claimId) throws SQLException {
        requireAdmin(admin);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Claim claim = claimDAO.findById(connection, claimId)
                        .orElseThrow(() -> new IllegalArgumentException("Claim not found."));

                if ("REJECTED".equalsIgnoreCase(claim.getStatus())) {
                    throw new IllegalArgumentException("This claim is already rejected.");
                }

                boolean updated = claimDAO.updateStatus(connection, claimId, "REJECTED");
                int activeClaims = claimDAO.countActiveClaimsForItem(connection, claim.getItemId());
                String itemStatus = activeClaims > 0 ? "CLAIMED" : "OPEN";
                boolean itemUpdated = itemDAO.updateStatus(connection, claim.getItemId(), itemStatus);

                if (!updated || !itemUpdated) {
                    throw new IllegalStateException("Unable to reject the selected claim.");
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void markReportResolved(User admin, int itemId) throws SQLException {
        requireAdmin(admin);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean itemUpdated = itemDAO.updateStatus(connection, itemId, "RESOLVED");
                claimDAO.rejectPendingClaimsByItem(connection, itemId);

                if (!itemUpdated) {
                    throw new IllegalStateException("Unable to mark the item as resolved.");
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void removeReport(User admin, int itemId) throws SQLException {
        requireAdmin(admin);
        String imagePath = null;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                imagePath = itemDAO.findById(connection, itemId)
                        .map(Item::getImagePath)
                        .orElse(null);
                claimDAO.deleteByItemId(connection, itemId);
                boolean deleted = itemDAO.deleteById(connection, itemId);

                if (!deleted) {
                    throw new IllegalStateException("Unable to remove the selected report.");
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }

        deleteImageQuietly(imagePath);
    }

    public void setUserRole(User admin, int userId, String role) throws SQLException {
        requireAdmin(admin);
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();

        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new IllegalArgumentException("Unsupported user role.");
        }

        if (admin.getUserId().equals(userId) && !"ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("You cannot remove your own admin role.");
        }

        if (!userDAO.updateRole(userId, normalizedRole)) {
            throw new IllegalStateException("Unable to update the selected user role.");
        }
    }

    public void setUserActive(User admin, int userId, boolean active) throws SQLException {
        requireAdmin(admin);

        if (admin.getUserId().equals(userId) && !active) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }

        if (!userDAO.updateActive(userId, active)) {
            throw new IllegalStateException("Unable to update the selected user account.");
        }
    }

    public int countReports(User admin) throws SQLException {
        requireAdmin(admin);
        return itemDAO.countAllItems();
    }

    public int countPendingClaims(User admin) throws SQLException {
        requireAdmin(admin);
        return claimDAO.countClaimsByStatus("PENDING");
    }

    public int countUsers(User admin) throws SQLException {
        requireAdmin(admin);
        return userDAO.countUsers();
    }

    public int countResolvedReports(User admin) throws SQLException {
        requireAdmin(admin);
        return itemDAO.countItemsByStatus("RESOLVED");
    }

    private void requireAdmin(User user) throws SQLException {
        if (user == null || user.getUserId() == null) {
            throw new SecurityException("Administrator access is required.");
        }

        User currentAdmin = userDAO.findById(user.getUserId())
                .orElseThrow(() -> new SecurityException("Administrator access is required."));

        if (!currentAdmin.isAdmin() || !currentAdmin.isActive()) {
            throw new SecurityException("Administrator access is required.");
        }
    }

    private void deleteImageQuietly(String imagePath) {
        try {
            ImageUtil.deleteLocalImage(imagePath);
        } catch (IOException ignored) {
            // Removing the database record is the primary admin action; image cleanup is best effort.
        }
    }
}
