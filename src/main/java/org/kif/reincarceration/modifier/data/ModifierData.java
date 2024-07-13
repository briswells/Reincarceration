package org.kif.reincarceration.modifier.data;

import org.bukkit.entity.Player;
import org.kif.reincarceration.data.DataManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModifierData {
    private final DataManager dataManager;

    public ModifierData(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void addCompletedModifier(Player player, String modifierId) throws SQLException {
        String sql = "INSERT INTO completed_modifiers (player_uuid, modifier_id) VALUES (?, ?)";
        try (Connection conn = dataManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, modifierId);
            pstmt.executeUpdate();
        }
    }

    public List<String> getCompletedModifiers(Player player) throws SQLException {
        List<String> completedModifiers = new ArrayList<>();
        String sql = "SELECT modifier_id FROM completed_modifiers WHERE player_uuid = ?";
        try (Connection conn = dataManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    completedModifiers.add(rs.getString("modifier_id"));
                }
            }
        }
        return completedModifiers;
    }

    public boolean hasCompletedModifier(Player player, String modifierId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM completed_modifiers WHERE player_uuid = ? AND modifier_id = ?";
        try (Connection conn = dataManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, modifierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // You might want to add methods for storing and retrieving active modifiers as well
}