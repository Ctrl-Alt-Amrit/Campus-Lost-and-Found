package com.lostfound.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseMigrator {
    private static final String ADMIN_EMAIL = "admin@campus.edu";
    private static final String DEFAULT_PASSWORD_HASH =
            "008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601";

    private DatabaseMigrator() {
    }

    public static void migrate() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            addColumnIfMissing(connection, "USERS", "ROLE",
                    "ALTER TABLE users ADD role VARCHAR2(20) DEFAULT 'USER'");
            addColumnIfMissing(connection, "USERS", "ACTIVE",
                    "ALTER TABLE users ADD active NUMBER(1) DEFAULT 1");

            executeUpdate(connection, "UPDATE users SET role = 'USER' WHERE role IS NULL");
            executeUpdate(connection, "UPDATE users SET active = 1 WHERE active IS NULL");
            executeIgnoringExpectedFailures(connection, "ALTER TABLE users MODIFY role DEFAULT 'USER' NOT NULL");
            executeIgnoringExpectedFailures(connection, "ALTER TABLE users MODIFY active DEFAULT 1 NOT NULL");

            addConstraintIfMissing(connection, "USERS", "CHK_USERS_ROLE",
                    "ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))");
            addConstraintIfMissing(connection, "USERS", "CHK_USERS_ACTIVE",
                    "ALTER TABLE users ADD CONSTRAINT chk_users_active CHECK (active IN (0, 1))");

            executeUpdate(connection, "UPDATE items SET status = 'OPEN' WHERE status IS NULL");
            refreshItemStatusConstraint(connection);
            ensureAdminUser(connection);
        }
    }

    public static void main(String[] args) throws SQLException {
        migrate();
    }

    private static void addColumnIfMissing(Connection connection, String tableName, String columnName, String sql)
            throws SQLException {
        if (!exists(connection,
                "SELECT COUNT(*) FROM user_tab_columns WHERE table_name = ? AND column_name = ?",
                tableName,
                columnName)) {
            executeUpdate(connection, sql);
        }
    }

    private static void addConstraintIfMissing(Connection connection, String tableName, String constraintName, String sql)
            throws SQLException {
        if (!exists(connection,
                "SELECT COUNT(*) FROM user_constraints WHERE table_name = ? AND constraint_name = ?",
                tableName,
                constraintName)) {
            executeUpdate(connection, sql);
        }
    }

    private static void refreshItemStatusConstraint(Connection connection) throws SQLException {
        for (String constraintName : findStatusCheckConstraints(connection, "ITEMS", "STATUS")) {
            executeUpdate(connection, "ALTER TABLE items DROP CONSTRAINT " + constraintName);
        }

        addConstraintIfMissing(connection, "ITEMS", "CHK_ITEMS_STATUS",
                "ALTER TABLE items ADD CONSTRAINT chk_items_status "
                        + "CHECK (status IN ('OPEN', 'CLAIMED', 'CLOSED', 'RESOLVED'))");
        executeIgnoringExpectedFailures(connection, "ALTER TABLE items MODIFY status DEFAULT 'OPEN' NOT NULL");
    }

    private static void ensureAdminUser(Connection connection) throws SQLException {
        if (!exists(connection, "SELECT COUNT(*) FROM users WHERE LOWER(email) = ?", ADMIN_EMAIL)) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (name, email, password, role, active) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, "System Administrator");
                statement.setString(2, ADMIN_EMAIL);
                statement.setString(3, DEFAULT_PASSWORD_HASH);
                statement.setString(4, "ADMIN");
                statement.setInt(5, 1);
                statement.executeUpdate();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET role = 'ADMIN', active = 1 WHERE LOWER(email) = ?")) {
                statement.setString(1, ADMIN_EMAIL);
                statement.executeUpdate();
            }
        }
    }

    private static boolean exists(Connection connection, String sql, String firstValue, String secondValue)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstValue);
            statement.setString(2, secondValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static boolean exists(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static List<String> findStatusCheckConstraints(Connection connection, String tableName, String columnName)
            throws SQLException {
        List<String> constraintNames = new ArrayList<>();
        String sql = """
                SELECT uc.constraint_name
                FROM user_constraints uc
                JOIN user_cons_columns ucc ON uc.constraint_name = ucc.constraint_name
                WHERE uc.table_name = ?
                  AND ucc.table_name = ?
                  AND ucc.column_name = ?
                  AND uc.constraint_type = 'C'
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, tableName);
            statement.setString(3, columnName);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    constraintNames.add(resultSet.getString("constraint_name"));
                }
            }
        }

        return constraintNames;
    }

    private static void executeUpdate(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void executeIgnoringExpectedFailures(Connection connection, String sql) throws SQLException {
        try {
            executeUpdate(connection, sql);
        } catch (SQLException exception) {
            String message = exception.getMessage();
            if (message == null || !message.contains("ORA-01442")) {
                throw exception;
            }
        }
    }
}
