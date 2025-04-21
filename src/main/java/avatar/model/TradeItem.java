package avatar.model;

import avatar.common.BossShopItem;
import avatar.constants.NpcName;
import avatar.handler.BossShopHandler;
import avatar.handler.UpgradeItemHandler;
import avatar.item.Item;
import avatar.item.PartManager;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class TradeItem extends BossShopItem {
    private Item item;
    private int coin;
    private int gold;
    public List<TradeItemNeed> itemNeeds;

    public TradeItem(Item itm, int coin, int gold, String itemNeedStr) {
        super(itm.getId(),itm.getId(),itm);
        this.item = itm;
        this.coin = coin;
        this.gold = gold;
        this.itemNeeds = parseItemNeed(itemNeedStr);
    }

    private List<TradeItemNeed> parseItemNeed(String itemNeedStr) {
        return TradeItemNeed.parseItemNeed(itemNeedStr);
    }

    public String getRequireText() {
        List<String> parts = new ArrayList<>();

        if (itemNeeds != null) {
            for (TradeItemNeed need : itemNeeds) {
                String name = PartManager.getInstance().findPartById(need.id).getName();
                if (need.quantity == 1) {
                    parts.add(name);
                } else {
                    parts.add(need.quantity + " " + name);
                }
            }
        }

        if (coin > 0) {
            parts.add(coin + " xu");
        }

        if (gold > 0) {
            parts.add(gold + " lượng");
        }

        return String.join(" + ", parts);
    }

    @Override
    public String initDialog(BossShop bossShop) {
        // Kiểm tra nếu item hợp lệ
        if (this.item == null) {
            return "Lỗi: Vật phẩm không hợp lệ.";
        }

        // Kiểm tra nếu item có phần hợp lệ
        if (this.item.getPart() == null) {
            return "Lỗi: Vật phẩm không có phần hợp lệ.";
        }

        if (bossShop.getIdBoss() == Npc.ID_ADD + 1214) {
            return MessageFormat.format(
                    "Bạn có muốn đổi {0} bằng {1} không?",
                    this.item.getPart().getName(),
                    getRequireText()
            );
        } else if (bossShop.getIdBoss() == Npc.ID_ADD + NpcName.TIEN_CA) {
            return MessageFormat.format(
                    "Bạn có muốn ghép {0} không?",
                    this.item.getPart().getName()
            );
        } else if (bossShop.getIdBoss() == Npc.ID_ADD + NpcName.PHU_THUY_NGUYEN_TO && bossShop.getIdShop() == 1) {
            return MessageFormat.format(
                    "Bạn có muốn đổi {0} bằng {1} không?",
                    this.item.getPart().getName(),
                    getRequireText()
            );
        } else if (bossShop.getIdBoss() == Npc.ID_ADD + NpcName.PHU_THUY_NGUYEN_TO && bossShop.getIdShop() == 2) {
            return MessageFormat.format(
                    "Bạn có muốn đổi {0} bằng {1} không?",
                    this.item.getPart().getName(),
                    getRequireText()
            );
        }

        return MessageFormat.format(
                "Bạn có muốn đổi {0} bằng {1} không?",
                this.item.getPart().getName(),
                getRequireText()
        );
    }
}
