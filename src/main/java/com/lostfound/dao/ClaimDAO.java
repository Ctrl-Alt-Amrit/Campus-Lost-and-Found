package com.lostfound.dao;

import com.lostfound.model.AdminClaimView;
import com.lostfound.model.Claim;
import com.lostfound.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimDAO {

    public boolean createClaim(Claim claim) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO claims (item_id, claimant_id, status) VALUES (?, ?, ?)")) {
            statement.setInt(1, claim.getItemId());
            statement.setInt(2, claim.getClaimantId());
            statement.setString(3, claim.getStatus());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean createClaim(Connection connection, Claim claim) throws SQLException {
        String sql = "INSERT INTO claims (item_id, claimant_id, status) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claim.getItemId());
            statement.setInt(2, claim.getClaimantId());
            statement.setString(3, claim.getStatus());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean hasExistingClaim(int itemId, int claimantId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM claims
                WHERE item_id = ? AND claimant_id = ? AND status IN ('PENDING', 'APPROVED')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            statement.setInt(2, claimantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public boolean hasExistingClaim(Connection connection, int itemId, int claimantId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM claims
                WHERE item_id = ? AND claimant_id = ? AND status IN ('PENDING', 'APPROVED')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            statement.setInt(2, claimantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    public int countClaimsByClaimant(int claimantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE claimant_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claimantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public List<AdminClaimView> findAllForAdmin() throws SQLException {
        List<AdminClaimView> claims = new ArrayList<>();
        String sql = """
                SELECT c.claim_id, c.item_id, i.title AS item_title, i.type AS item_type,
                       i.status AS item_status, i.location, c.claimant_id,
                       claimant.name AS claimant_name, claimant.email AS claimant_email,
                       reporter.name AS reporter_name, c.status
                FROM claims c
                JOIN items i ON c.item_id = i.item_id
                JOIN users claimant ON c.claimant_id = claimant.user_id
                JOIN users reporter ON i.user_id = reporter.user_id
                ORDER BY CASE c.status WHEN 'PENDING' THEN 1 WHEN 'APPROVED' THEN 2 ELSE 3 END,
                         c.claim_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                claims.add(new AdminClaimView(
                        resultSet.getInt("claim_id"),
                        resultSet.getInt("item_id"),
                        resultSet.getString("item_title"),
                        resultSet.getString("item_type"),
                        resultSet.getString("item_status"),
                        resultSet.getString("location"),
                        resultSet.getInt("claimant_id"),
                        resultSet.getString("claimant_name"),
                        resultSet.getString("claimant_email"),
                        resultSet.getString("reporter_name"),
                        resultSet.getString("status")
                ));
            }
        }

        return claims;
    }

    public Optional<Claim> findById(Connection connection, int claimId) throws SQLException {
        String sql = "SELECT claim_id, item_id, claimant_id, status FROM claims WHERE claim_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claimId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapClaim(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public boolean updateStatus(Connection connection, int claimId, String status) throws SQLException {
        String sql = "UPDATE claims SET status = ? WHERE claim_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, claimId);
            return statement.executeUpdate() > 0;
        }
    }

    public int rejectOtherPendingClaims(Connection connection, int itemId, int approvedClaimId) throws SQLException {
        String sql = """
                UPDATE claims
                SET status = 'REJECTED'
                WHERE item_id = ? AND claim_id <> ? AND status = 'PENDING'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            statement.setInt(2, approvedClaimId);
            return statement.executeUpdate();
        }
    }

    public int rejectPendingClaimsByItem(Connection connection, int itemId) throws SQLException {
        String sql = """
                UPDATE claims
                SET status = 'REJECTED'
                WHERE item_id = ? AND status = 'PENDING'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            return statement.executeUpdate();
        }
    }

    public int countActiveClaimsForItem(Connection connection, int itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND status IN ('PENDING', 'APPROVED')";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public int countClaimsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE status = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public int deleteByItemId(Connection connection, int itemId) throws SQLException {
        String sql = "DELETE FROM claims WHERE item_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            return statement.executeUpdate();
        }
    }

    private Claim mapClaim(ResultSet resultSet) throws SQLException {
        return new Claim(
                resultSet.getInt("claim_id"),
                resultSet.getInt("item_id"),
                resultSet.getInt("claimant_id"),
                resultSet.getString("status")
        );
    }
}
