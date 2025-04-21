package avatar.model;

import avatar.common.BossShopItem;
import avatar.constants.NpcName;
import avatar.handler.BossShopHandler;
import avatar.handler.UpgradeItemHandler;
import avatar.item.PartManager;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.text.MessageFormat;

@Getter
@Setter
@SuperBuilder
public class ShopNpc extends BossShopItem {
    private int itemneed;
    private int xuneed;
    private int luongneed;
    private int point;

    @Override
    public String initDialog(BossShop bossShop) {
        if(xuneed <= 0) {
        return MessageFormat.format(
                    "Bạn có muốn mua {0} bằng {1} lượng không ?",
                    super.getItem().getPart().getName(),
                    luongneed
            );
        }
        else if(luongneed <= 0) {
            return MessageFormat.format(
                    "Bạn có muốn mua {0} bằng {1} xu không ?",
                    super.getItem().getPart().getName(),
                    xuneed
            );
        }
        else {
            return MessageFormat.format(
                    "Bạn có muốn mua {0} bằng {1} xu và {2} lượng không ?",
                    super.getItem().getPart().getName(),
                    xuneed,
                    luongneed
            );
        }
    };
}