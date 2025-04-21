package avatar.model;

import java.util.ArrayList;
import java.util.List;

public class TradeItemNeed {
    public int id;
    public int quantity;

    public TradeItemNeed(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }



    public static List<TradeItemNeed> parseItemNeed(String itemNeedStr) {
        List<TradeItemNeed> itemNeeds = new ArrayList<>();
        try {
            itemNeedStr = itemNeedStr.replace("\"", "");
            itemNeedStr = itemNeedStr.trim();
            if (itemNeedStr.startsWith("[") && itemNeedStr.endsWith("]")) {
                itemNeedStr = itemNeedStr.substring(1, itemNeedStr.length() - 1);
            }
            String[] itemPairs = itemNeedStr.split(",");

            for (String pair : itemPairs) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    int itemId = Integer.parseInt(parts[0].trim());
                    int quantity = Integer.parseInt(parts[1].trim());

                    itemNeeds.add(new TradeItemNeed(itemId, quantity));
                } else {
                    System.out.println("[ERROR] : " + pair);
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR]  itemNeed: " + itemNeedStr);
            e.printStackTrace();
        }
        return itemNeeds;
    }


}