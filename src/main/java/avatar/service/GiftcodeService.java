package avatar.service;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import avatar.model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import avatar.item.Item;
import avatar.db.DbManager;
import org.apache.log4j.Logger;
import java.sql.SQLException;

public class GiftcodeService {
    private static final Logger logger = Logger.getLogger(GiftcodeService.class);
    private static GiftcodeService instance;
    private final JSONParser parser = new JSONParser();

    public static GiftcodeService gI() {
        if (instance == null) {
            instance = new GiftcodeService();
        }
        return instance;
    }

    public void handleGiftcode(User user, String code) {
        Connection conn = null;
        try {
            conn = DbManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            String query = "SELECT * FROM giftcode WHERE code = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, code);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    // Kiểm tra số lần sử dụng
                    String usedByStr = rs.getString("used_by");
                    JSONArray usedBy = usedByStr != null ?
                    (JSONArray) parser.parse(usedByStr) : new JSONArray();

                        if (usedBy.size() >= rs.getInt("max_use")) {
                            user.getService().serverDialog("Giftcode đã hết lượt sử dụng!");
                            return;
                        }

                    if (usedBy.stream()
                            .anyMatch(id -> ((Long) id).intValue() == user.getId())) {
                            user.getService().serverDialog("Bạn đã sử dụng giftcode này rồi!");
                            return;
                    }
                    StringBuilder rewardMessage = new StringBuilder();
                    rewardMessage.append("Giftcode: ").append(code).append("\n");
                    rewardMessage.append("Phần thưởng:\n");

                    // Xử lý phần thưởng
                    // Phần thưởng chung
                    JSONObject commonRewards = (JSONObject) parser.parse(rs.getString("rewards_items"));
                    addRewards(user, commonRewards, rewardMessage);

                    // Cập nhật người dùng đã sử dụng
                    usedBy.add(user.getId());
                    String updateQuery = "UPDATE giftcode SET used_by = ? WHERE code = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateQuery)) {
                        updatePs.setString(1, usedBy.toJSONString());
                        updatePs.setString(2, code);
                        updatePs.executeUpdate();
                    }

                    conn.commit();
                    // Hiển thị thông tin phần thưởng
                    user.getAvatarService().customTab("Giftcode", rewardMessage.toString());
                        
                } else {
                    user.getService().serverDialog("Giftcode " + code + " không tồn tại!");
                }
            }
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
        }
    }

    private void addRewards(User user, JSONObject rewards, StringBuilder rewardMessage) {
        try {
            int totalItems = 0;
            if (rewards.containsKey("items")) {
                JSONArray items = (JSONArray) rewards.get("items");
                totalItems += items.size();
            }

            synchronized (user.getChests()) {
                if (user.getChests().size() + totalItems > user.getChestSlot()) {
                    user.getService().serverDialog("Rương đã đầy, không thể nhận thêm vật phẩm!");
                    return;
                }

                if (rewards.containsKey("xu")) {
                    int xu = ((Long)rewards.get("xu")).intValue();
                    user.updateXu(xu);
                    user.getAvatarService().updateMoney(0);
                    rewardMessage.append("- ").append(xu).append(" xu\n");
                }
                if (rewards.containsKey("luong")) {
                    int luong = ((Long)rewards.get("luong")).intValue();
                    user.updateLuong(luong);
                    user.getAvatarService().updateMoney(1);
                    rewardMessage.append("- ").append(luong).append(" lượng\n");
                }
                if (rewards.containsKey("items")) {
                    JSONArray items = (JSONArray) rewards.get("items");
                    addItems(user, items, rewardMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Error adding rewards", e);
        }
    }

    private void addItems(User user, JSONArray items, StringBuilder rewardMessage) {
        for (Object obj : items) {
            JSONObject item = (JSONObject) obj;
            int itemId = ((Long)item.get("id")).intValue();
            int quantity = ((Long)item.get("quantity")).intValue();
            int expireDay = -1;

            Item newItem;
            if (item.containsKey("expireDay")) {
                int days = ((Long)item.get("expireDay")).intValue();
                if (days > 0) {
                    long expireTime = System.currentTimeMillis() + (days * 86400000L);
                    newItem = new Item(itemId, -1, quantity);
                    newItem.setExpired(expireTime);
                    rewardMessage.append("- ").append(quantity).append(" x ").append(newItem.getPart().getName())
                            .append(" (").append(days).append(" ngày)\n");
                } else {
                    newItem = new Item(itemId, expireDay, quantity);
                    rewardMessage.append("- ").append(quantity).append(" x ").append(newItem.getPart().getName()).append("\n");
                }
            } else {
                newItem = new Item(itemId, expireDay, quantity);
                rewardMessage.append("- ").append(quantity).append(" x ").append(newItem.getPart().getName()).append("\n");
            }
            user.addItemToChests(newItem);
        }
    }
}