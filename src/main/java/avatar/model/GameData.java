package avatar.model;

import avatar.Farm.FarmAnimal;
import avatar.Farm.FarmItems;
import avatar.Farm.FarmTree;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import avatar.db.DbManager;
import lombok.Getter;

@Getter
public class GameData {
    private static final GameData instance = new GameData();

    public static final GameData getInstance() {
        return instance;
    }

    private List<ImageInfo> itemImageDatas = new ArrayList<>();
    private List<ImageInfo> farmImageDatas = new ArrayList<>();
    private List<MapItem> mapItems = new ArrayList<>();
    private List<MapItemType> mapItemTypes = new ArrayList<>();
    private List<FarmItems> farmItems = new ArrayList<>();
    private List<FarmTree> farmTree = new ArrayList<>();
    private List<FarmAnimal> farmAnimal = new ArrayList<>();


    public void load() {
        loadItemImageData();
        loadFarmImageData();
        loadMapItem();
        loadMapItemType();
        loadFarmItems();
        loadFarmTree();
        loadFarmAnimal();
    }


    public void loadFarmItems() {
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
                farmItems.add(FarmItems.builder().id(id).idimg(idimg).type(type).action(action).description(description).pricexu(pricexu).priceluong(priceluong).isitem(isitem).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmTree() {
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
                farmTree.add(FarmTree.builder().id(id).name(name).harvesttime(harvesttime).pricexu(pricexu).priceluong(priceluong).productid(productid).numproduct(numproduct).level(level).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFarmAnimal() {
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
                farmAnimal.add(FarmAnimal.builder().species(species).name(name).pricexu(pricexu).priceluong(priceluong).harvesttime(harvesttime).numproduct(numproduct).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadItemImageData() {
        itemImageDatas.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `avatar_img_data`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("item_id");
                int bigImageID = rs.getInt("image_id");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int w = rs.getInt("w");
                int h = rs.getInt("h");
                itemImageDatas.add(ImageInfo.builder().id(id).bigImageID(bigImageID).x(x).y(y).w(w).h(h).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadFarmImageData() {
        farmImageDatas.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `farm_image_data`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int bigImageID = rs.getInt("image_id");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int w = rs.getInt("w");
                int h = rs.getInt("h");
                farmImageDatas.add(
                        ImageInfo.builder().id(id).bigImageID(bigImageID).x(x).y(y).w(w).h(h).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadMapItem() {
        mapItems.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `map_item`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                short id = rs.getShort("id");
                short typeID = rs.getShort("type_id");
                byte type = rs.getByte("type");
                byte x = rs.getByte("x");
                byte y = rs.getByte("y");
                mapItems.add(MapItem.builder().id(id).typeID(typeID).type(type).x(x).y(y).build());
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadMapItemType() {
        mapItemTypes.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `map_item_type`;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                short id = rs.getShort("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                short imageID = rs.getShort("image");
                short iconID = rs.getShort("icon");
                short priceCoin = rs.getShort("price_coin");
                short priceGold = rs.getShort("price_gold");
                byte buy = rs.getByte("buy");
                byte dx = rs.getByte("dx");
                byte dy = rs.getByte("dy");
                JSONArray jPosition = (JSONArray) JSONValue.parse(rs.getString("position"));
                int size = jPosition.size();
                List<Position> positions = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    JSONObject obj = (JSONObject) jPosition.get(i);
                    byte x = ((Long) obj.get("x")).byteValue();
                    byte y = ((Long) obj.get("y")).byteValue();
                    Position p = Position.builder()
                            .x(x)
                            .y(y)
                            .build();
                    positions.add(p);
                }
                mapItemTypes.add(MapItemType.builder().id(id).name(name).des(description).imgID(imageID).iconID(iconID)
                        .priceXu(priceCoin).priceLuong(priceGold).buy(buy).dx(dx).dy(dy).listNotTrans(positions)
                        .build());
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MapItemType findMapItemType(int idType) {
        for (MapItemType mapItemType : mapItemTypes) {
            if (mapItemType.getId() == idType) {
                return mapItemType;
            }
        }
        return null;
    }
}
