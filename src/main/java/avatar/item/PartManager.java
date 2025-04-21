package avatar.item;

import avatar.Farm.FarmAnimal;
import avatar.Farm.FarmCooking;
import avatar.Farm.FarmItems;
import avatar.Farm.FarmTree;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import avatar.Farm.farmwarehouseitem;
import avatar.model.UpgradeItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import avatar.db.DbManager;
import avatar.model.ShopNpc;
import avatar.model.TradeItem;
import lombok.Getter;

public class PartManager {

    private static final PartManager instance = new PartManager();

    public static PartManager getInstance() {
        return instance;
    }

    @Getter
    private final List<FarmItems> farmItems = new ArrayList<>();
    private final List<FarmTree> farmTree = new ArrayList<>();
    private final List<FarmAnimal> farmAnimal = new ArrayList<>();
    private final List<farmwarehouseitem> farmwarehouseitems = new ArrayList<>();
    private final List<FarmCooking> farmcookings = new ArrayList<>();


    @Getter
    private final List<Part> parts = new ArrayList<>();
    @Getter
    private final List<UpgradeItem> upgradeItems = new ArrayList<>();
    @Getter
    private final List<TradeItem> tradeItems = new ArrayList<>();
    @Getter
    private final List<ShopNpc> shopNpcItems = new ArrayList<>();
    @Getter
    private final List<Part> ShopHawaii1 = new ArrayList<>();///type 19 shop 1
    @Getter
    private final List<Part> Shop1 = new ArrayList<>();///type 19 shop 1
    @Getter
    private final List<Part> ShopHawaii2 = new ArrayList<>();///type 14 shop 2
    @Getter
    private final List<Part> ShopPremium = new ArrayList<>(); // 17
    @Getter
    private final List<Part> ShopXeng = new ArrayList<>(); // 23
    @Getter
    private final List<Part>ShopPet = new ArrayList<>();///type 13 shop thú cưng
    @Getter
    private final List<Part>Shop4 = new ArrayList<>();///type 9 vé số
    @Getter
    private final List<Part>Gift = new ArrayList<>();///type 3 quà tặng
    //private final List<UpgradeItem> upgradeItems = new ArrayList<>();


    public Part findPartById(int id) {
        return getParts().stream()
                .filter(part -> part.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void load() {
        parts.clear();
        ShopPremium.clear();
        ShopXeng.clear();
        ShopHawaii1.clear();
        Shop1.clear();
        ShopHawaii2.clear();
        ShopPet.clear();
        Gift.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `items`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int coin = rs.getInt("coin");
                int gold = rs.getInt("gold");
                short type = rs.getShort("type");
                String name = rs.getString("name");
                short icon = rs.getShort("icon");
                int expiredDay = rs.getInt("expired_day");
                byte level = rs.getByte("level");
                byte sell = rs.getByte("sell");
                byte zOrder = rs.getByte("zorder");
                byte gender = rs.getByte("gender");
                short[] imgID = new short[15];
                byte[] dx = new byte[15];
                byte[] dy = new byte[15];
                JSONArray animation = (JSONArray) JSONValue.parse(rs.getString("animation"));
                int size = animation.size();
                for (int i = 0; i < size; i++) {
                    JSONObject obj = (JSONObject) animation.get(i);
                    imgID[i] = ((Long) obj.get("img")).shortValue();
                    dx[i] = ((Long) obj.get("dx")).byteValue();
                    dy[i] = ((Long) obj.get("dy")).byteValue();
                }
                parts.add(Part.builder().id(id)
                        .coin(coin)
                        .gold(gold)
                        .type(type)
                        .name(name)
                        .icon(icon)
                        .expiredDay(expiredDay)
                        .level(level)
                        .sell(sell)
                        .zOrder(zOrder)
                        .gender(gender)
                        .imgID(imgID)
                        .dx(dx)
                        .dy(dy)
                        .build());
                // Hiện thông tin load vật phẩm ra output
                //System.out.println("id: " + id + " name: " + name);
                if (sell == 14) {
                    ShopHawaii1.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 19) {
                    Shop1.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 15) {
                    ShopHawaii2.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 17) {
                    ShopPremium.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 23) {
                    ShopXeng.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 13) {
                    ShopPet.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 9) {
                    Shop4.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
                if (sell == 3) {
                    Gift.add(Part.builder().id(id)
                            .coin(coin)
                            .gold(gold)
                            .type(type)
                            .name(name)
                            .icon(icon)
                            .expiredDay(expiredDay)
                            .level(level)
                            .sell(sell)
                            .zOrder(zOrder)
                            .gender(gender)
                            .imgID(imgID)
                            .dx(dx)
                            .dy(dy)
                            .build());// Add the individual Part object
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadUpgradeItemData();
        loadTradeItemData();
        loadFarmItemData(); // data shop nông sản
        loadFarmTreeData(); // data cây trồng
        loadFarmAnimalData(); // data vật nuôi
        loadFarmWarehouseItemData(); // data nông sản
        loadFarmCookingData(); // data nấu ăn
        loadShopNpcItemData();
        System.out.println("Tai danh sach vat pham thanh cong");
    }
    public void loadFarmItemData() {
        farmItems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `farmitems`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int idimg = rs.getInt("imageid");
                int type = rs.getInt("type");
                String action = rs.getString("action");
                String description = rs.getString("description");
                int pricexu = rs.getInt("pricexu");
                int priceluong = rs.getInt("priceluong");
                int isitem = rs.getInt("isitem");
                farmItems.add(FarmItems
                        .builder()
                        .id(id)
                        .idimg(idimg)
                        .type(type)
                        .action(action)
                        .description(description)
                        .pricexu(pricexu)
                        .priceluong(priceluong)
                        .isitem(isitem)
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmTreeData() {
        farmTree.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `farmtree`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int harvesttime = rs.getInt("harvest_time");
                int pricexu = rs.getInt("price_xu");
                int priceluong = rs.getInt("price_luong");
                int productid = rs.getInt("product_id");
                int numproduct = rs.getInt("num_product");
                int level = rs.getInt("level");
                farmTree.add(FarmTree
                        .builder()
                        .id(id)
                        .name(name)
                        .harvesttime(harvesttime)
                        .pricexu(pricexu)
                        .priceluong(priceluong)
                        .productid(productid)
                        .numproduct(numproduct)
                        .level(level)
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmAnimalData() {
        farmAnimal.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `animals`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int species = rs.getInt("species");
                String name = rs.getString("name");
                int pricexu = rs.getInt("price_xu");
                int priceluong = rs.getInt("price_luong");
                int harvesttime = rs.getInt("harvest_time");
                int numproduct = rs.getInt("num_product");
                farmAnimal.add(FarmAnimal
                        .builder()
                        .species(species)
                        .name(name)
                        .pricexu(pricexu)
                        .priceluong(priceluong)
                        .harvesttime(harvesttime)
                        .numproduct(numproduct)
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmWarehouseItemData() {
        farmwarehouseitems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `farm_warehouse_item`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_item");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                farmwarehouseitems.add(farmwarehouseitem
                        .builder()
                        .id(id)
                        .name(name)
                        .price(price)
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmCookingData() {
        farmcookings.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `farmcooking`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int require1 = rs.getInt("require1");
                int quantity1 = rs.getInt("quantity1");
                int require2 = rs.getInt("require2");
                int quantity2 = rs.getInt("quantity2");
                int time = rs.getInt("time");
                int product = rs.getInt("product");
                farmcookings.add(FarmCooking
                        .builder()
                        .id(id)
                        .name(name)
                        .require1(require1)
                        .quantity1(quantity1)
                        .require2(require2)
                        .quantity2(quantity2)
                        .time(time)
                        .product(product)
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }




    public void loadUpgradeItemData() {
        upgradeItems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `upgrade_item`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int itemId = rs.getInt("item_id");
                boolean onlyLuong = rs.getInt("is_only_luong") == 1;
                int ratio = rs.getInt("ratio");
                int itemNeed = rs.getInt("item_need");
                int luong = rs.getInt("luong");
                int xu = rs.getInt("xu");
                int scores = rs.getInt("scores");
                upgradeItems.add(UpgradeItem
                        .builder()
                        .id(id)
                        .itemRequest(itemId)
                        .itemNeed(itemNeed)
                        .ratio(ratio)
                        .luong(luong)
                        .isOnlyLuong(onlyLuong)
                        .xu(xu)
                        .scores(scores)
                        .item(new Item(itemId))
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadTradeItemData() {
        tradeItems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `shop_trade`;");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("item_id");  // item_id
                int coin = rs.getInt("coin");
                int gold = rs.getInt("gold");
                String itemNeedStr = rs.getString("itemNeed");


                // Kiểm tra giá trị của itemNeedStr
                if (itemNeedStr == null || itemNeedStr.isEmpty()) {
                    System.out.println("[ERROR] itemNeedStr is empty for ID: " + id);
                    continue; // Skip this iteration if itemNeedStr is empty or invalid
                }

                // Tạo TradeItem với item hợp lệ
                TradeItem tradeItem = new TradeItem(new Item(id), coin, gold, itemNeedStr);

                // Kiểm tra item hợp lệ trước khi thêm vào tradeItems
                if (tradeItem.getItem() == null) {
                    System.out.println("[ERROR] TradeItem không hợp lệ với ID: " + id);
                    continue;
                }

                // Kiểm tra nếu tradeItem có itemNeeds hợp lệ
                if (tradeItem.getItemNeeds() == null || tradeItem.getItemNeeds().isEmpty()) {
                    System.out.println("[ERROR] itemNeeds không hợp lệ cho TradeItem với ID: " + id);
                    continue;
                }

                tradeItems.add(tradeItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void loadShopNpcItemData() {
        shopNpcItems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `shop_npc`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int itemId = rs.getInt("item_id");
                int xuneed = rs.getInt("xu");
                int luongneed = rs.getInt("luong");
                shopNpcItems.add(ShopNpc
                        .builder()
                        .id(id)
                        .itemRequest(itemId)
                        .xuneed(xuneed)
                        .luongneed(luongneed)
                        .item(new Item(itemId))
                        .build()
                );
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Part> getAvatarPart() {
        return parts.stream().filter((t) -> t.getId() < 2000).collect(Collectors.toList());
    }

    public Part findPartByID(int id) {
        for (Part part : parts) {
            if (part.getId() == id) {
                return part;
            }
        }
        return null;
    }

    public FarmItems findFarmitemByID(int id) {
        for (FarmItems FarmItems : farmItems) {
            if (FarmItems.getId() == id) {
                return FarmItems;
            }
        }
        return null;
    }
    
    public FarmTree findFarmtreeByID(int id) {
        for (FarmTree FarmTree : farmTree) {
            if (FarmTree.getId() == id) {
                return FarmTree;
            }
        }
        return null;
    }
    
    public FarmAnimal findFarmAnimalByID(int id) {
        for (FarmAnimal FarmAnimal : farmAnimal) {
            if (FarmAnimal.getSpecies() == id) {
                return FarmAnimal;
            }
        }
        return null;
    }
    
    public farmwarehouseitem findFarmwarehouseitemByID(int id) {
        for (farmwarehouseitem farmwarehouseitem : farmwarehouseitems) {
            if (farmwarehouseitem.getId() == id) {
                return farmwarehouseitem;
            }
        }
        return null;
    }
    
     public FarmCooking findFarmCookingByID(int id) {
        for (FarmCooking farmcooking : farmcookings) {
            if (farmcooking.getId() == id) {
                return farmcooking;
            }
        }
        return null;
    }

}