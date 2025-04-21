package avatar.handler;

import avatar.common.BossShopItem;
import avatar.constants.NpcName;
import avatar.item.PartManager;
import avatar.model.BossShop;
import avatar.model.Npc;
import avatar.model.User;
import avatar.service.AvatarService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class ShopTradeHandler {

    public static void displayUI(User us, int type, int npcID, int... itemIds) {
        AvatarService service = us.getAvatarService();

        List<Integer> itemIdList = IntStream.of(itemIds)
                .boxed()
                .collect(Collectors.toList());

        List<BossShopItem> TradeShop = PartManager.getInstance().getTradeItems()
                .stream()
                .filter(tradeItem -> tradeItem != null && tradeItem.getItem() != null)  // Kiểm tra xem tradeItem và item có null không
                .filter(tradeItem -> itemIdList.contains(tradeItem.getItem().getId()))
                .sorted(Comparator.comparingInt(item -> {
                    int index = itemIdList.indexOf(item.getItem().getId());
                    return index == -1 ? Integer.MAX_VALUE : index;
                }))
                .collect(Collectors.toList());

        us.setBossShopItems(TradeShop);

        System.out.println("Danh sách tất cả item trong TradeItems:");
        for (BossShopItem item : PartManager.getInstance().getTradeItems()) {
            if (item == null || item.getItem() == null) {
                System.err.println("[ERROR] Item null trong TradeItems!");
            } else {
                System.out.println("Item ID: " + item.getItem().getId());
            }
        }

        service.openUIShopTrade(
                BossShop.builder()
                        .idBoss(npcID + Npc.ID_ADD)
                        .idShop((byte) type)
                        .typeShop((byte) 0)
                        .name("Trao đổi")
                        .build(),
                TradeShop
        );
    }


    public static void handle() {

    }
}
