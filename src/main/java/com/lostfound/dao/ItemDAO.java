package com.lostfound.dao;

import com.lostfound.model.AdminItemView;
import com.lostfound.model.Item;
import com.lostfound.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemDAO {

    public boolean createItem(Item item) throws SQLException {
        String sql = """
                INSERT INTO items (user_id, type, title, description, location, item_date, image_path, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getUserId());
            statement.setString(2, item.getType());
            statement.setString(3, item.getTitle());
            statement.setString(4, item.getDescription());
            statement.setString(5, item.getLocation());
            statement.setDate(6, Date.valueOf(item.getItemDate()));
            statement.setString(7, item.getImagePath());
            statement.setString(8, item.getStatus());
            return statement.executeUpdate() > 0;
        }
    }

    public List<Item> searchItems(String keyword) throws SQLException {
        List<Item> items = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        boolean hasKeyword = !normalizedKeyword.isEmpty();

        String sql = hasKeyword
                ? """
                  SELECT *
                  FROM (
                      SELECT item_id, user_id, type, title, description, location, item_date, image_path, status
                      FROM items
                      WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ?
                      ORDER BY item_date DESC, item_id DESC
                  )
                  WHERE ROWNUM <= 100
                  """
                : """
                  SELECT *
                  FROM (
                      SELECT item_id, user_id, type, title, description, location, item_date, image_path, status
                      FROM items
                      ORDER BY item_date DESC, item_id DESC
                  )
                  WHERE ROWNUM <= 100
                  """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (hasKeyword) {
                String likeValue = "%" + normalizedKeyword + "%";
                statement.setString(1, likeValue);
                statement.setString(2, likeValue);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }
        }

        return items;
    }

    public Optional<Item> findById(int itemId) throws SQLException {
        String sql = """
                SELECT item_id, user_id, type, title, description, location, item_date, image_path, status
                FROM items
                WHERE item_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapItem(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Item> findById(Connection connection, int itemId) throws SQLException {
        String sql = """
                SELECT item_id, user_id, type, title, description, location, item_date, image_path, status
                FROM items
                WHERE item_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapItem(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<AdminItemView> findAllForAdmin() throws SQLException {
        List<AdminItemView> items = new ArrayList<>();
        String sql = """
                SELECT i.item_id, i.type, i.title, i.location, i.item_date, i.status,
                       u.name AS reporter_name, u.email AS reporter_email,
                       (SELECT COUNT(*) FROM claims c WHERE c.item_id = i.item_id) AS claim_count
                FROM items i
                JOIN users u ON i.user_id = u.user_id
                ORDER BY i.item_date DESC, i.item_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                items.add(new AdminItemView(
                        resultSet.getInt("item_id"),
                        resultSet.getString("type"),
                        resultSet.getString("title"),
                        resultSet.getString("location"),
                        resultSet.getDate("item_date").toLocalDate(),
                        resultSet.getString("status"),
                        resultSet.getString("reporter_name"),
                        resultSet.getString("reporter_email"),
                        resultSet.getInt("claim_count")
                ));
            }
        }

        return items;
    }

    public int countItemsByTypeAndStatus(String type, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE type = ? AND status = ?";
        return executeCountQuery(sql, type, status);
    }

    public int countItemsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE status = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public int countAllItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM items";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public int countItemsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public boolean updateStatus(int itemId, String status) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE items SET status = ? WHERE item_id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection connection, int itemId, String status) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatusIfCurrent(Connection connection, int itemId, String currentStatus, String newStatus)
            throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE item_id = ? AND status = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setInt(2, itemId);
            statement.setString(3, currentStatus);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Connection connection, int itemId) throws SQLException {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    private int executeCountQuery(String sql, String type, String status) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type);
            statement.setString(2, status);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private Item mapItem(ResultSet resultSet) throws SQLException {
        Item item = new Item();
        item.setItemId(resultSet.getInt("item_id"));
        item.setUserId(resultSet.getInt("user_id"));
        item.setType(resultSet.getString("type"));
        item.setTitle(resultSet.getString("title"));
        item.setDescription(resultSet.getString("description"));
        item.setLocation(resultSet.getString("location"));
        item.setItemDate(resultSet.getDate("item_date").toLocalDate());
        item.setImagePath(resultSet.getString("image_path"));
        item.setStatus(resultSet.getString("status"));
        return item;
    }
}
