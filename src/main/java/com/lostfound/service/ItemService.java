package com.lostfound.service;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.model.Claim;
import com.lostfound.model.Item;
import com.lostfound.model.User;
import com.lostfound.util.DBConnection;
import com.lostfound.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();
    private final ClaimDAO claimDAO = new ClaimDAO();

    public void reportItem(User currentUser, String type, String title, String description,
                           String location, LocalDate itemDate, String imagePath) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("You must be logged in to report an item.");
        }

        if (!currentUser.isActive()) {
            throw new IllegalStateException("Your account is disabled.");
        }

        if (ValidationUtil.isBlank(type) || ValidationUtil.isBlank(title)
                || ValidationUtil.isBlank(description) || ValidationUtil.isBlank(location)
                || itemDate == null) {
            throw new IllegalArgumentException("Please complete all required item fields.");
        }

        String normalizedType = type.trim().toUpperCase();
        String normalizedTitle = title.trim();
        String normalizedDescription = description.trim();
        String normalizedLocation = location.trim();

        if (!"LOST".equals(normalizedType) && !"FOUND".equals(normalizedType)) {
            throw new IllegalArgumentException("Item type must be LOST or FOUND.");
        }

        if (normalizedTitle.length() > 150) {
            throw new IllegalArgumentException("Title must be 150 characters or fewer.");
        }

        if (normalizedDescription.length() > 500) {
            throw new IllegalArgumentException("Description must be 500 characters or fewer.");
        }

        if (normalizedLocation.length() > 150) {
            throw new IllegalArgumentException("Location must be 150 characters or fewer.");
        }

        if (itemDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Item date cannot be in the future.");
        }

        Item item = new Item();
        item.setUserId(currentUser.getUserId());
        item.setType(normalizedType);
        item.setTitle(normalizedTitle);
        item.setDescription(normalizedDescription);
        item.setLocation(normalizedLocation);
        item.setItemDate(itemDate);
        item.setImagePath(imagePath);
        item.setStatus("OPEN");

        boolean created = itemDAO.createItem(item);
        if (!created) {
            throw new IllegalStateException("Unable to save the item report.");
        }
    }

    public List<Item> searchItems(String keyword) throws SQLException {
        return itemDAO.searchItems(keyword);
    }

    public Item getItemById(int itemId) throws SQLException {
        return itemDAO.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));
    }

    public void claimItem(Item item, User claimant) throws SQLException {
        if (item == null || claimant == null) {
            throw new IllegalArgumentException("Item and claimant details are required.");
        }

        if (!claimant.isActive()) {
            throw new IllegalStateException("Your account is disabled.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Item currentItem = itemDAO.findById(connection, item.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Item not found."));

                if (currentItem.getUserId().equals(claimant.getUserId())) {
                    throw new IllegalArgumentException("You cannot claim your own item.");
                }

                if (!"FOUND".equalsIgnoreCase(currentItem.getType())) {
                    throw new IllegalArgumentException("Only found items can be claimed.");
                }

                if (!"OPEN".equalsIgnoreCase(currentItem.getStatus())) {
                    throw new IllegalArgumentException("This item is no longer available for claim.");
                }

                if (claimDAO.hasExistingClaim(connection, currentItem.getItemId(), claimant.getUserId())) {
                    throw new IllegalArgumentException("You have already submitted a claim for this item.");
                }

                Claim claim = new Claim();
                claim.setItemId(currentItem.getItemId());
                claim.setClaimantId(claimant.getUserId());
                claim.setStatus("PENDING");

                boolean claimCreated = claimDAO.createClaim(connection, claim);
                boolean itemUpdated = itemDAO.updateStatusIfCurrent(
                        connection,
                        currentItem.getItemId(),
                        "OPEN",
                        "CLAIMED"
                );

                if (!claimCreated || !itemUpdated) {
                    connection.rollback();
                    throw new IllegalStateException("This item is no longer available for claim.");
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public int countOpenLostItems() throws SQLException {
        return itemDAO.countItemsByTypeAndStatus("LOST", "OPEN");
    }

    public int countOpenFoundItems() throws SQLException {
        return itemDAO.countItemsByTypeAndStatus("FOUND", "OPEN");
    }

    public int countUserReports(int userId) throws SQLException {
        return itemDAO.countItemsByUser(userId);
    }

    public int countUserClaims(int userId) throws SQLException {
        return claimDAO.countClaimsByClaimant(userId);
    }
}
