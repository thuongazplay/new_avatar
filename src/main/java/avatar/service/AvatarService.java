package avatar.service;
import java.io.*;
import java.lang.reflect.Field;
import avatar.common.BossShopItem;
import avatar.db.DbManager;
import avatar.handler.BossShopHandler;
import avatar.handler.ShopEventHandler;
import avatar.item.Item;
import avatar.item.PartManager;
import avatar.item.Part;
import avatar.lucky.DialLuckyManager;
import avatar.message.MessageHandler;
import avatar.message.ParkMsgHandler;
import avatar.model.*;
import avatar.server.Avatar;
import avatar.server.ServerManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import avatar.constants.Cmd;
import avatar.network.Message;
import avatar.network.Session;
import avatar.play.Map;
import avatar.play.Zone;
import avatar.server.UserManager;
import java.util.Random;
import lombok.Builder;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AvatarService extends Service {

    private static final Logger logger = Logger.getLogger(AvatarService.class);
    private static final java.util.Map<Integer, Long> lastActionTimes = new HashMap<>();
    private static final long ACTION_COOLDOWN_MS = 50; // 2 giây cooldown
    public AvatarService(Session cl) {
        super(cl);
    }

    public User user;
    public void openUIShop(int id, String name, List<Item> items) {
        try {
            System.out.println("openShop lent: " + items.size());
            Message ms = new Message(Cmd.OPEN_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeShort(items.size());
            for (Item i : items) {
                ds.writeShort(i.getId());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }
    public void openGiftShop() {
        try {
            Message ms = new Message(Cmd.OPEN_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(26);
            ds.writeUTF("Quà");
            List<Item> gift = Part.shopByPart(PartManager.getInstance().getGift());
            ds.writeShort(gift.size()); // tổng số item
            for (Item i : gift) {
                ds.writeShort(i.getId());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void openUIShopEvent(BossShop bossShop, List<BossShopItem> items) {
        try {
            System.out.println("openShop bossShop: " + items.size());
            Message ms = new Message(Cmd.BOSS_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossShop.getTypeShop());
            ds.writeInt(bossShop.getIdBoss());
            ds.writeByte(bossShop.getIdShop());
            ds.writeUTF(bossShop.getName());
            ds.writeShort(items.size());
            for (BossShopItem item : items) {
                ds.writeShort(item.getItemRequest());
                ds.writeUTF(item.initDialog(bossShop));
                if (bossShop.getTypeShop() == 1) {
                    ds.writeUTF(item.initDialog(bossShop));
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }
    
    public void openUIShopTrade(BossShop bossShop, List<BossShopItem> items) {
        try {
            System.out.println("TradeShop: " + items.size());
            Message ms = new Message(Cmd.BOSS_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossShop.getTypeShop());
            ds.writeInt(bossShop.getIdBoss());
            ds.writeByte(bossShop.getIdShop());
            ds.writeUTF(bossShop.getName());
            ds.writeShort(items.size());
            for (BossShopItem item : items) {
                ds.writeShort(item.getItemRequest());
                ds.writeUTF(item.initDialog(bossShop));
                if (bossShop.getTypeShop() == 1) {
                    ds.writeUTF(item.initDialog(bossShop));
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }
    
    public void openUIShopNpc(BossShop bossShop, List<BossShopItem> items) {
        try {
            System.out.println("NpcShop: " + items.size());
            Message ms = new Message(Cmd.BOSS_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossShop.getTypeShop());
            ds.writeInt(bossShop.getIdBoss());
            ds.writeByte(bossShop.getIdShop());
            ds.writeUTF(bossShop.getName());
            ds.writeShort(items.size());
            for (BossShopItem item : items) {
                ds.writeShort(item.getItemRequest());
                ds.writeUTF(item.initDialog(bossShop));
                if (bossShop.getTypeShop() == 1) {
                    ds.writeUTF(item.initDialog(bossShop));
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void openUIBossShop(BossShop bossShop, List<BossShopItem> items) {
        try {
            System.out.println("openShop bossShop: " + items.size());
            Message ms = new Message(Cmd.BOSS_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossShop.getTypeShop());
            ds.writeInt(bossShop.getIdBoss());
            ds.writeByte(bossShop.getIdShop());
            ds.writeUTF(bossShop.getName());
            ds.writeShort(items.size());
            for (BossShopItem item : items) {
                ds.writeShort(item.getItemRequest());
                ds.writeUTF(item.initDialog(bossShop));
                if (bossShop.getTypeShop() == 1) {
                    ds.writeUTF(item.initDialog(bossShop));
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void doRequestExpicePet(Message mss) {
        try {
            int userID = mss.reader().readInt();
            Message ms = new Message(Cmd.REQUEST_EXPICE_PET);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(0);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void showUICreateChar(byte type) {
        try {
            Message ms = new Message(Cmd.CREATE_CHAR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("showUICreateChar ", ex);
        }
    }

    public void viewChest(List<Item> chests) {
        try {
            Message ms = new Message(Cmd.CONTAINER);
            DataOutputStream ds = ms.writer();
            ds.writeShort(chests.size());
            for (Item item : chests) {
                ds.writeShort(item.getId());
                ds.writeByte(100 - item.reliability());
                ds.writeUTF(item.expiredString());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("viewChest ", e);
        }
    }

    public void chatTo(String sender, String content,int type) {
        try {
            Message ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            ds.writeInt(type);
            ds.writeUTF(sender);
            ds.writeUTF(content);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("chatTo ", ex);
        }
    }

    public void chatToUser(Message ms) {
        try {
            int receiverId = ms.reader().readInt();
            String content = ms.reader().readUTF();
            int senderId = this.session.user.getId(); // ID của người gửi yêu cầu
            User receiver = UserManager.getInstance().find(receiverId);
            User sender = UserManager.getInstance().find(senderId);
            receiver.getAvatarService().chatTo(sender.getUsername(), content,senderId);
            
        } catch (IOException ex) {
            logger.error("chatTo ", ex);
        }
    }

    public void onLoginSuccess() {
        try {
            User us = session.user;
            List<Item> wearing = us.getWearing();
            List<Command> listCmd = us.getListCmd();
            List<Command> listCmdRotate = us.getListCmdRotate();
            Message ms5 = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms5.writer();
            ds.writeInt(us.getId());
            ds.writeByte(wearing.size());
            for (Item itm : wearing) {
                ds.writeShort(itm.getId());
            }
            ds.writeByte(us.getGender());
            ds.writeByte(us.getLeverMain());
            ds.writeByte(us.getLeverMainPercen());
            ds.writeInt(Math.toIntExact(us.getXu()));
            ds.writeByte(us.getFriendly());
            ds.writeByte(10);//us.getCrazy()
            ds.writeByte(100);//us.getStylish()
            ds.writeByte(100);//us.getHappy()
            ds.writeByte(100 - us.getHunger());
            ds.writeInt(us.getLuong());
            ds.writeByte(us.getStar()); // hiện icon Admin
            for (Item itm : wearing) {
                ds.writeByte(1);
                ds.writeUTF(itm.expiredString());
            }
            String sql = "SELECT c.icon, c.description FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE cm.user_id = ? AND cm.accept = 1";
            try (Connection connection = DbManager.getInstance().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, us.getId());  // Giả sử us.getId() trả về ID của user hiện tại
                try (ResultSet res = ps.executeQuery()) {
                    if (res.next()) {
                        // Nếu người dùng tham gia vào một clan, lấy thông tin
                        short icon = res.getShort("icon");
                        String thongbaonhom = res.getString("description");
                        ds.writeShort(icon);  // Ghi ID icon của clan vào DataOutputStream
                        us.getAvatarService().SendTabmsg("Thông báo nhóm: " + thongbaonhom);  // Gửi thông báo nhóm
                    } else {
                        // Nếu không có kết quả (người dùng không tham gia clan nào)
                        ds.writeShort((short) -1);  // Ghi giá trị mặc định -1 cho icon
                        us.getAvatarService().SendTabmsg("Bạn chưa tham gia vào nhóm nào.");  // Gửi thông báo mặc định
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);  // Xử lý lỗi SQL nếu có
            }



            ds.writeByte(listCmd.size());
            for (Command cmd : listCmd) {
                ds.writeUTF(cmd.getName());
                ds.writeShort(cmd.getIcon());
            }
            ds.writeByte(listCmdRotate.size());
            for (Command cmd : listCmdRotate) {
                ds.writeShort(cmd.getAnthor());
                ds.writeUTF(cmd.getName());
                ds.writeShort(cmd.getIcon());
            }
            ds.writeBoolean(true);// isTour
            for (Command cmd : listCmdRotate) {
                ds.writeByte(cmd.getType());
            }
            ds.writeByte(1);
            ds.writeShort(us.getLeverMain());

            //hẹn hò
            if(us.getIdUsHenHo() !=0&&us.getLevelMarry() ==0){
                ds.writeShort(2);
                us.setTenNhan("Cặp đôi hẹn hò");
                us.setImginfo(1114);
            } else if (us.getLevelMarry() > 0 && us.getLevelMarry()<5) {
                ds.writeShort(1153);
                us.setTenNhan("Cặp đôi mới cưới");
                us.setImginfo(1106);
            } else if (us.getLevelMarry() > 4 && us.getLevelMarry()<10) {
                ds.writeShort(1154);
                us.setTenNhan("Cặp đôi gì đó lv hơn 5 dưới 10");
                us.setImginfo(1107);
            } else if (us.getLevelMarry() > 9 && us.getLevelMarry()<15) {
                ds.writeShort(1155);
                us.setTenNhan("Cặp đôi gì đó lv hơn 10 dưới 15");
                us.setImginfo(1108);
            }else if (us.getLevelMarry() > 14 && us.getLevelMarry()<20) {
                ds.writeShort(1156);
                us.setTenNhan("Cặp đôi gì đó lv hơn 15 dưới 20");
                us.setImginfo(1109);
            } else if (us.getLevelMarry() > 19 && us.getLevelMarry()<24) {
                ds.writeShort(1157);
                us.setTenNhan("Cặp đôi gì đó lv hơn 5 dưới 10");
                us.setImginfo(1110);
            }else
                ds.writeShort(1096);
            
            ds.writeBoolean(session.isNewVersion());//new version
            if (session.isNewVersion()) {
                ds.writeInt(us.getXeng());
            }
            int m = 4;
            ds.writeByte((byte) m);
            short[] IDAction = {103, 102, 104, 107};
            String[] actionName = new String[]{"Tặng Hoa Violet", "Hôn", "Tặng cánh hoa",
                    "Tặng Hoa Tuyết"};
            short[] IDIcon = {1124, 1188, 1187, 1173};
            int[] money = {20000, 2000, 10000, 5};
            byte[] typeMoney = {0, 0, 0, 1};
            for (int i2 = 0; i2 < m; ++i2) {
                ds.writeShort(IDAction[i2]);
                ds.writeUTF(actionName[i2]);
                ds.writeShort(IDIcon[i2]);
                ds.writeInt(money[i2]);
                ds.writeByte(typeMoney[i2]);
            }
            ds.writeInt(us.getLuong());
            ds.writeInt(us.getLuongKhoa());
            ds.writeByte(1);
            ds.writeUTF(us.getUsername());
            ds.flush();
            sendMessage(ms5);

            us.getAvatarService().SendTabmsg("Chơi game điều độ, giữ gìn sức khỏe, chơi game quá 180 phút mỗi ngày sẽ có hại cho sức khỏe");
//            us.getAvatarService().SendTabmsg("donate cho admin update game di qua group zalo");
            us.getAvatarService().SendTabmsg("Bạn có " + us.getLuong() + " lượng và " + us.getLuongKhoa() + " lượng khóa.");

        } catch (IOException ex) {
            logger.error("onLoginSuccess err", ex);
        }
    }

    public void SendTabmsg(String content) throws IOException {
        Message ms = new Message(-6);
        DataOutputStream ds = ms.writer();
        ds = ms.writer();
        ds.writeInt(1);
        ds.writeUTF("admin");
        ds.writeUTF(content);
        ds.flush();
        this.session.sendMessage(ms);
    }
    public void SendTabFriend(int idtab, String user, String content) throws IOException {
        Message ms = new Message(-6);
        DataOutputStream ds = ms.writer();
        ds = ms.writer();
        ds.writeInt(2);
        ds.writeUTF(user);
        ds.writeUTF(content);
        ds.flush();
        this.session.sendMessage(ms);
    }
    public void getAvatarPart() {
        try {
            List<Part> parts = PartManager.getInstance().getAvatarPart();
            Message ms = new Message(Cmd.GET_AVATAR_PART);
            DataOutputStream ds = ms.writer();
            ds.writeShort(parts.size());
            for (Part part : parts) {
                ds.writeShort(part.getId());
                ds.writeInt(part.getCoin());
                ds.writeShort(part.getGold());
                short type = part.getType();
                ds.writeShort(type);
                switch (type) {
                    case -2:
                        ds.writeUTF(part.getName());
                        ds.writeByte(part.getSell());
                        ds.writeShort(part.getIcon());
                        break;

                    case -1:
                        ds.writeUTF(part.getName());
                        ds.writeByte(part.getSell());
                        ds.writeByte(part.getZOrder());
                        ds.writeByte(part.getGender());
                        ds.writeByte(part.getLevel());
                        ds.writeShort(part.getIcon());
                        short[] imgID = part.getImgID();
                        byte[] dx = part.getDx();
                        byte[] dy = part.getDy();
                        for (int i = 0; i < 15; i++) {
                            ds.writeShort(imgID[i]);
                            ds.writeByte(dx[i]);
                            ds.writeByte(dy[i]);
                        }
                        break;

                    default:
                        ds.writeShort(part.getIcon());
                        break;
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            logger.error("getAvatarPart() ", e);
        }
    }


    public void inspectMessageData(Message message) {
        DataInputStream dis = message.reader();
        if (dis != null) {
            try {
                while (dis.available() > 0) {  // Vòng lặp cho đến khi hết dữ liệu
                    try {
                        boolean b = dis.readBoolean();
                        System.out.println("Read int: " + b);
                        int intValue = dis.readInt();  // Thử đọc int
                        System.out.println("Read int: " + intValue);
                    } catch (IOException e) {
                        // Nếu không phải int, hãy thử kiểu dữ liệu khác
                        try {
                            String stringValue = dis.readUTF();  // Thử đọc chuỗi
                            System.out.println("Read string: " + stringValue);
                        } catch (IOException ex) {
                            try {
                                byte byteValue = dis.readByte();  // Thử đọc byte
                                System.out.println("Read byte: " + byteValue);
                            } catch (IOException exc) {
                                System.out.println("Unknown data format or end of data.");
                                break;  // Nếu tất cả các thử nghiệm đều thất bại, kết thúc vòng lặp
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading message data: " + e.getMessage());
            }
        } else {
            System.err.println("DataInputStream is null.");
        }
    }

    /**
     * Lấy thông tin item và giá tiền để in lên shop?
     *
     * @param ms
     */
    public void requestJoinAny(Message ms) throws IOException {
        byte id = ms.reader().readByte();
        byte idSelectedMini = ms.reader().readByte();
        short idJoin = ms.reader().readShort();
        int mapId = this.session.user.getZone().getMap().getId();
        System.out.println("JOIN ANY: "+id+" "+idSelectedMini+" "+idJoin);

        switch (idJoin) {
            case 4:
                if(mapId == 9 || mapId == 27) {
                    ms = new Message(Cmd.JOIN_ONGAME_MINI);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(1);
                    ds.writeByte(0);
                    ds.writeShort(4);
                    this.session.sendMessage(ms);
                }
                else {
                    sendMap37();
                }
                break;
            case 5:
                if(mapId == 27) {
                List<Item> items = Part.shopByPart(PartManager.getInstance().getShopHawaii1());
                if (items == null) {
                    System.out.println("Null Shop Hawaii 1");
                    return; // Handle the null case
                }
                this.session.user.getAvatarService().openUIShop(5, "Đồ bơi", items);
                }
                else {
                    sendMap38();
                }
                break;
            case 6:
                sendMap39();
                break;
            case 7:
                sendMap40();
                break;
            case 8:
                sendMap41();
                break;
            case 9:
                if(mapId == 27) {
                List<Item> itemshop2 = Part.shopByPart(PartManager.getInstance().getShopHawaii2());
                if (itemshop2 == null) {
                    System.out.println("Null Shop Hawaii 2");
                    return; // Handle the null case
                }
                this.session.user.getAvatarService().openUIShop(5, "Đồ chơi", itemshop2);
                }
                else {
                    sendMap42();
                }
                break;
            case 10:
                sendMap43();
                break;
            case 11:
                sendMap33();
                break;
            case 17:
                if(mapId == 28) {
                    sendMap27();
                }
                else if(mapId == 34) {
                    sendMap33();
                }
                else {
                    sendMap55();
                }
                break;
            case 18:
                if(mapId == 27 || mapId == 29 || mapId == 30) {
                    sendMap28();
                }
                else if(mapId == 33 || mapId == 35) {
                    sendMap34();
                }
                else if(mapId == 55) {
                    sendMap56();
                }
                break;
            case 19:
                if(mapId == 34 || mapId == 36 || mapId == 39){
                    sendMap35();
                }
                else if(mapId == 28) {
                    sendMap29();
                }
                else {
                    sendMap57();
                }
                break;
            case 20:
                if(mapId == 28){
                    sendMap30();
                }
                else if(mapId == 35 || mapId == 39) {
                    sendMap36();
                }
                else {
                    //this.session.user.getAvatarService().serverDialog("Đang bảo trì");
                    sendMap58();
                }
                break;
            // Add more cases as needed
            default:
                this.session.user.getZone().leave(this.session.user);
                //this.session.user.getAvatarService().serverDialog("Map chưa hoàn thiện "+idJoin);
                break;
        }

    }
    
    private void sendMap27() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map27 = Avatar.getFile(folder + "mapdata_27.dat");
        byte[] map_bg27 = Avatar.getFile(folder + "/bg/27.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(27);
        ds.writeByte(1);
        ds.writeShort(306);
        ds.writeByte(34);
        ds.writeShort(map27.length);
        ds.write(map27);

        short[] arr = {828, -1, 835, 853, 852, 836, 832, 832, 851, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg27.length);
        ds.write(map_bg27);

        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 27;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {26, 27, 28, 29, 31, 64};
                byte[] mapItemB = {0, 0, 0, 0, 0, 4};
                byte[] mapItemC = {2, 7, 27, 20, 33, 15};
                byte[] mapItemD = {1, 1, 1, 1, 1, 0};


                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ds.flush();
        this.session.sendMessage(ms);
    }

    
    private void sendMap28() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map28 = Avatar.getFile(folder + "mapdata_28.dat");
        byte[] map_bg28 = Avatar.getFile(folder + "mapimage_28.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(28);
        ds.writeByte(1);
        ds.writeShort(306);
        ds.writeByte(27);
        ds.writeShort(map28.length);
        ds.write(map28);

        short[] arr = {828, -1, 835, 853, 852, 836, 832, 832, 851, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg28.length);
        ds.write(map_bg28);

        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 28;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {0, 1, 2, 3, 4, 5, 6, 7, 22, 23, 30, 32, 33, 34, 38, 39, 40};
                byte[] mapItemB = {0, 0, 0, 1, 2, 2, 3, 3, 5, 6, 0, 7, 7, 7, 7, 8, 8};
                byte[] mapItemC = {22, 24, 3, 14, 0, 23, 5, 12, 1, 25, 2, 9, 7, 20, 18, 19, 8};
                byte[] mapItemD = {3, 7, 4, 3, 3, 3, 5, 7, 7, 7, 7, 3, 3, 6, 6, 6, 3};


                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap29() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map29 = Avatar.getFile(folder + "mapdata_29.dat");
        byte[] map_bg29 = Avatar.getFile(folder + "mapimage_29.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(29);
        ds.writeByte(1);
        ds.writeShort(306);
        ds.writeByte(16);
        ds.writeShort(map29.length);
        ds.write(map29);

        short[] arr = {828, -1, 835, 853, 852, 836, 832, 832, 851, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg29.length);
        ds.write(map_bg29);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 29;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {8, 9, 10, 11, 12, 24, 35, 36, 37, 41, 42, 43, 44};
                byte[] mapItemB = {0, 3, 3, 2, 0, 6, 7, 7, 0, 8, 7, 7, 8};
                byte[] mapItemC = {1, 14, 4, 1, 2, 14, 4, 6, 13, 5, 9, 11, 10};
                byte[] mapItemD = {7, 7, 7, 3, 4, 3, 5, 5, 3, 5, 3, 3, 3};


                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap30() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map30 = Avatar.getFile(folder + "mapdata_30.dat");
        byte[] map_bg30 = Avatar.getFile(folder + "mapimage_30.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(30);
        ds.writeByte(1);
        ds.writeShort(306);
        ds.writeByte(16);
        ds.writeShort(map30.length);
        ds.write(map30);

        short[] arr = {828, -1, 835, 853, 852, 836, 832, 832, 851, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg30.length);
        ds.write(map_bg30);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 30;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {13, 14, 15, 16, 25, 45};
                byte[] mapItemB = {2, 2, 2, 3, 5, 3};
                byte[] mapItemC = {15, 9, 4, 3, 1, 14};
                byte[] mapItemD = {3, 4, 7, 3, 3, 6};


                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap33() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map33 = Avatar.getFile(folder + "mapdata_33.dat");
        byte[] map_bg33 = Avatar.getFile(folder + "mapimage_33.png");
        
        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(33);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(25);
        ds.writeShort(map33.length);
        ds.write(map33);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg33.length);
        ds.write(map_bg33);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 33;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56};
                byte[] mapItemB = {9, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11};
                byte[] mapItemC = {10, 16, 17, 19, 21, 23, 8, 6, 4, 2, 0};
                byte[] mapItemD = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                
                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap34() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map34 = Avatar.getFile(folder + "mapdata_34.dat");
        byte[] map_bg34 = Avatar.getFile(folder + "/mapimage_34.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(34);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(27);
        ds.writeShort(map34.length);
        ds.write(map34);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg34.length);
        ds.write(map_bg34);

        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 34;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {57, 58, 59, 60, 61, 62, 63};
                byte[] mapItemB = {12, 13, 14, 14, 13, 15, 15};
                byte[] mapItemC = {15, 17, 18, 4, 6, 24, 23};
                byte[] mapItemD = {5, 4, 2, 2, 1, 2, 5};



                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap35() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map35 = Avatar.getFile(folder + "mapdata_35.dat");
        byte[] map_bg35 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(35);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(17);
        ds.writeShort(map35.length);
        ds.write(map35);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg35.length);
        ds.write(map_bg35);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap36() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map36 = Avatar.getFile(folder + "mapdata_36.dat");
        byte[] map_bg36 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(36);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(20);
        ds.writeShort(map36.length);
        ds.write(map36);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg36.length);
        ds.write(map_bg36);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap37() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map37 = Avatar.getFile(folder + "mapdata_37.dat");
        byte[] map_bg37 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(37);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(22);
        ds.writeShort(map37.length);
        ds.write(map37);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg37.length);
        ds.write(map_bg37);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap38() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map38 = Avatar.getFile(folder + "mapdata_38.dat");
        byte[] map_bg38 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(38);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(21);
        ds.writeShort(map38.length);
        ds.write(map38);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg38.length);
        ds.write(map_bg38);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap39() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map39 = Avatar.getFile(folder + "mapdata_39.dat");
        byte[] map_bg39 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(39);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(20);
        ds.writeShort(map39.length);
        ds.write(map39);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg39.length);
        ds.write(map_bg39);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap40() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map40 = Avatar.getFile(folder + "mapdata_40.dat");
        byte[] map_bg40 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(40);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(20);
        ds.writeShort(map40.length);
        ds.write(map40);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg40.length);
        ds.write(map_bg40);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap41() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map41 = Avatar.getFile(folder + "mapdata_41.dat");
        byte[] map_bg41 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(41);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(19);
        ds.writeShort(map41.length);
        ds.write(map41);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg41.length);
        ds.write(map_bg41);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap42() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map42 = Avatar.getFile(folder + "mapdata_42.dat");
        byte[] map_bg42 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(42);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(19);
        ds.writeShort(map42.length);
        ds.write(map42);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg42.length);
        ds.write(map_bg42);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap43() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map43 = Avatar.getFile(folder + "mapdata_43.dat");
        byte[] map_bg43 = Avatar.getFile(folder + "/mapimage_55.png");


        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(43);
        ds.writeByte(2);
        ds.writeShort(306);
        ds.writeByte(21);
        ds.writeShort(map43.length);
        ds.write(map43);

        short[] arr = {-1, -1, 835, -1, -1, -1, -1, -1, -1, -1, -1, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg43.length);
        ds.write(map_bg43);
        ds.writeShort(0);
           

        ds.flush();
        this.session.sendMessage(ms);
    }
    
    private void sendMap55() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map55 = Avatar.getFile(folder + "mapdata_55.dat");
        byte[] map_bg55 = Avatar.getFile(folder + "mapimage_55.png");
        
        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(55);
        ds.writeByte(4);
        ds.writeShort(306);
        ds.writeByte(26);
        ds.writeShort(map55.length);
        ds.write(map55);

        short[] arr = {828, -1, 835, -1, -1, -1, -1, -1, -1, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg55.length);
        ds.write(map_bg55);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 55;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {65, 106, 107, 108, 109, 110, 111, 112, 122, 123, 124, 125, 126, 127, -128, -127, -126, -125, -124};
                byte[] mapItemB = {42, 42, 19, 18, 20, 20, 16, 16, 17, 16, 21, 21, 21, 21, 22, 22, 16, 16, 16};
                byte[] mapItemC = {16, 1, 12, 6, 1, 23, 0, 9, 18, 20, 0, 7, 22, 25, 8, 25, 4, 16, 24};
                byte[] mapItemD = {0, 0, 0, 1, 1, 1, 2, 2, 1, 2, 5, 3, 1, 4, 5, 2, 5, 5, 5};

                
                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap56() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map56 = Avatar.getFile(folder + "mapdata_56.dat");
        byte[] map_bg56 = Avatar.getFile(folder + "mapimage_55.png");
        
        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(56);
        ds.writeByte(4);
        ds.writeShort(435);
        ds.writeByte(29);
        ds.writeShort(map56.length);
        ds.write(map56);

        short[] arr = {828, -1, 835, -1, -1, -1, -1, -1, -1, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg56.length);
        ds.write(map_bg56);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 56;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87};
                byte[] mapItemB = {31, 30, 30, 28, 28, 27, 23, 23, 22, 22, 25, 26, 26, 40, 40, 40, 29, 29, 29, 29, 44, 44};
                byte[] mapItemC = {14, 9, 19, 10, 18, 13, 4, 23, 1, 24, 8, 3, 20, 7, 9, 25, 2, 9, 19, 26, 6, 22};
                byte[] mapItemD = {2, 3, 3, 3, 3, 4, 5, 5, 11, 12, 9, 11, 8, 4, 12, 8, 5, 5, 5, 5, 1, 1};

                
                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap57() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map57 = Avatar.getFile(folder + "mapdata_57.dat");
        byte[] map_bg57 = Avatar.getFile(folder + "mapimage_55.png");
        
        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(57);
        ds.writeByte(4);
        ds.writeShort(294);
        ds.writeByte(21);
        ds.writeShort(map57.length);
        ds.write(map57);

        short[] arr = {828, -1, 835, -1, -1, -1, -1, -1, -1, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg57.length);
        ds.write(map_bg57);
        try {
            Connection connection = DbManager.getInstance().getConnection();
            int mapId = 56;

            String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_typem` WHERE `map_id` = ?";
            PreparedStatement ps = connection.prepareStatement(GET_MAP_ITEM_TYPE,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, mapId);

            ResultSet res = ps.executeQuery();
            if (res != null) {
                ds.writeShort(204);
                res.last();
                int rows = res.getRow();
                ds.writeByte(rows);
                res.beforeFirst();

                while (res.next()) {
                    ds.writeByte(res.getByte("id_type"));
                    ds.writeShort(res.getShort("id_img"));
                    ds.writeByte(res.getByte("icon_id"));
                    ds.writeShort(res.getShort("dx"));
                    ds.writeShort(res.getShort("dy"));


                    JSONArray av_position = (JSONArray) JSONValue.parse(res.getString("av_position"));
                    ds.writeByte(av_position.size());
                    for (int m = 0; m < av_position.size(); m++) {
                        JSONObject av_position_element = (JSONObject) av_position.get(m);
                        ds.writeByte(((Long) av_position_element.get("x")).shortValue());
                        ds.writeByte(((Long) av_position_element.get("y")).shortValue());
                    }
                }
                byte[] mapItemA = {88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 120, 121};
                byte[] mapItemB = {34, 33, 35, 36, 37, 22, 22, 40, 40, 41, 40, 25, 26, 44, 44};
                byte[] mapItemC = {10, 3, 8, 13, 16, 2, 14, 5, 15, 18, 2, 6, 18, 4, 17};
                byte[] mapItemD = {8, 4, 4, 3, 4, 6, 7, 4, 10, 4, 9, 8, 8, 1, 1};


                
                ds.writeByte(mapItemA.length);
                for (int k = 0; k < mapItemA.length; k++) {
                    ds.writeByte(mapItemA[k]);
                    ds.writeByte(mapItemB[k]);
                    ds.writeByte(mapItemC[k]);
                    ds.writeByte(mapItemD[k]);
                }
            } else {
                ds.writeShort(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.session.sendMessage(ms);
    }
    
    private void sendMap58() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map58 = Avatar.getFile(folder + "mapdata_58.dat");
        byte[] map_bg58 = Avatar.getFile(folder + "mapimage_55.png");
        
        Message ms = new Message(-93);
        DataOutputStream ds = ms.writer();

        ds.writeByte(58);
        ds.writeByte(4);
        ds.writeShort(336);
        ds.writeByte(21);
        ds.writeShort(map58.length);
        ds.write(map58);

        short[] arr = {828, -1, 835, -1, -1, -1, -1, -1, -1, 833, 837, 842, 843, -1, -1};
        ds.writeByte(arr.length);
        for (int j = 0; j < arr.length; j++) {
            ds.writeShort(arr[j]);
        }

        ds.writeShort(map_bg58.length);
        ds.write(map_bg58);
        ds.writeShort(0);
           
        this.session.sendMessage(ms);
    }






    public void requestPartDynaMic(Message ms) {
        try {
            short itemID = ms.reader().readShort();
            Part part = PartManager.getInstance().findPartByID(itemID);
            // cmd -97
            ms = new Message(Cmd.REQUEST_DYNAMIC_PART);
            DataOutputStream ds = ms.writer();
            ds.writeShort(part.getId());
            ds.writeInt(part.getCoin());
            ds.writeShort(part.getGold());
            short type = part.getType();
            ds.writeShort(type);
            switch (type) {
                case -2:
                    ds.writeUTF(part.getName());
                    ds.writeByte(part.getSell());
                    ds.writeShort(part.getIcon());
                    break;

                case -1:
                    ds.writeUTF(part.getName());
                    ds.writeByte(part.getSell());
                    ds.writeByte(part.getZOrder());
                    ds.writeByte(part.getGender());
                    ds.writeByte(part.getLevel());
                    ds.writeShort(part.getIcon());
                    short[] imgID = part.getImgID();
                    byte[] dx = part.getDx();
                    byte[] dy = part.getDy();
                    for (int i = 0; i < 15; i++) {
                        ds.writeShort(imgID[i]);
                        ds.writeByte(dx[i]);
                        ds.writeByte(dy[i]);
                    }
                    break;
                default:
                    ds.writeShort(part.getIcon());
                    break;
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException ex) {
            logger.error("requestPartDynaMic() ", ex);
        }
    }

     public void enter(Zone z) {
        try {
            List<User> players = z.getPlayers();
            Map map = z.getMap();
            Message ms = new Message(Cmd.AVATAR_JOIN_PARK);
            DataOutputStream ds = ms.writer();
            ds.writeByte(map.getId());
            ds.writeByte(z.getId());
            ds.writeShort(-1);
            ds.writeShort(-1);
            int numUser = players.size();
            ds.writeByte((byte) numUser);
            for (User pl : players) {
                ds.writeInt(pl.getId());
                ds.writeUTF(pl.getUsername());
                ds.writeByte(pl.getWearing().size());
                for (Item item : pl.getWearing()) {
                    ds.writeShort(item.getId());
                }
                ds.writeShort(pl.getX());
                ds.writeShort(pl.getY());
                ds.writeByte(pl.getStar());//0 la npc
            }
            for (User pl : players) {
                ds.writeByte(pl.getDirect());
            }
            for (int i = 0; i < numUser; ++i) {
                ds.writeByte(101);
            }
            for (User pl : players) {
                String sql = "SELECT c.icon, c.description FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE cm.user_id = ? AND cm.accept = 1";
            try (Connection connection = DbManager.getInstance().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, pl.getId());  // Giả sử us.getId() trả về ID của user hiện tại
                try (ResultSet res = ps.executeQuery()) {
                    if (res.next()) {
                        // Nếu người dùng tham gia vào một clan, lấy thông tin
                        short icon = res.getShort("icon");
                        ds.writeShort(icon);
                    } else {
                        // Nếu không có kết quả (người dùng không tham gia clan nào)
                        ds.writeShort((short) -1);  // Ghi giá trị mặc định -1 cho icon
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);  // Xử lý lỗi SQL nếu có
            }
            }
            ds.writeByte(0);
            ds.writeByte(0);


            List<MapItem> mapItems = map.getMapItems();
            List<MapItemType> mapItemTypes = map.getMapItemTypes();
            ds.writeShort(mapItems.size());
            ds.writeByte(mapItemTypes.size());
            for (MapItemType mapItemType : mapItemTypes) {
                ds.writeByte(mapItemType.getId());
                ds.writeShort(mapItemType.getImgID());
                ds.writeByte(mapItemType.getIconID());
                ds.writeShort(mapItemType.getDx());
                ds.writeShort(mapItemType.getDy());
                List<Position> positions = mapItemType.getListNotTrans();
                ds.writeByte(positions.size());
                for (Position position : positions) {
                    ds.writeByte(position.getX());
                    ds.writeByte(position.getY());
                }
            }
            ds.writeByte(mapItems.size());
            for (MapItem mapItem : mapItems) {
                ds.writeByte(mapItem.getType());
                ds.writeByte(mapItem.getTypeID());
                ds.writeByte(mapItem.getX());
                ds.writeByte(mapItem.getY());
            }
            for (int i = 0; i < numUser; ++i) {
                ds.writeShort(-1);
            }
            ds.flush();

//            if (map.getId() != 17) {
//                ds.writeShort(0);
//            } else {
//                ds.writeShort(224);
//                short[] objectID = new short[]{11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791, 11791};
//                short[] objectX = new short[]{-8, -9, -15, 0, 0, -20, 0, -26, 0, 0, 0, -10, -2, -22, -4, -5};
//                short[] objectY = new short[]{-37, -58, -8, -33, 0, -6, 0, -23, 0, 0, -9, -12, -15, -22, -13, -9};
//                byte[][] obj_a = new byte[][]{{0, 1, -1, 2}, new byte[0], new byte[0], {0}, {0, 3}, new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], {0}, {0}, {0, 1}, {-1, 0, 1, 2}, {0}, {0}};
//                byte[][] obj_b = new byte[][]{{0, 0, 0, 0}, new byte[0], new byte[0], {0}, {0, 0}, new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], {0}, {0}, {0, 0}, {0, 0, 0, 0}, {0}, {0}};
//                int numObject = 16;
//                ds.writeByte(numObject);
//
//                for(int j = 0; j < numObject; ++j) {
//                    ds.writeByte(j);
//                    ds.writeShort(objectID[j]);
//                    ds.writeByte(0);
//                    ds.writeShort(objectX[j]);
//                    ds.writeShort(objectY[j]);
//                    byte nObj = (byte)obj_a[j].length;
//                    ds.writeByte(nObj);
//
//                    for(int m = 0; m < nObj; ++m) {
//                        ds.writeByte(obj_a[j][m]);
//                        ds.writeByte(obj_b[j][m]);
//                    }
//                }
//
//                byte[] mapItemA = new byte[]{70, 71, 72, 111, 112, 113, 114, 115, 116, 117, 118};
//                byte[] mapItemB = new byte[]{1, 1, 1, 1, 11, 11, 11, 10, 10, 10, 12};
//                byte[] mapItemC = new byte[]{1, 18, 10, 23, 2, 9, 21, 5, 24, 17, 13};
//                byte[] mapItemD = new byte[]{1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 3};
//                ds.writeByte(mapItemA.length);
//
//                for(int k = 0; k < mapItemA.length; ++k) {
//                    ds.writeByte(mapItemA[k]);
//                    ds.writeByte(mapItemB[k]);
//                    ds.writeByte(mapItemC[k]);
//                    ds.writeByte(mapItemD[k]);
//                }
//            }
//
//            for(int i = 0; i < numUser; ++i) {
//                ds.writeShort(-1);
//            }
//
//            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("enter() ", ex);
        }
    }

    public void getImageData() {
        try {
            List<ImageInfo> imageInfos = GameData.getInstance().getItemImageDatas();
            Message ms = new Message(Cmd.GET_IMAGE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(imageInfos.size());
            for (ImageInfo imageInfo : imageInfos) {
                ds.writeShort(imageInfo.getId());
                ds.writeShort(imageInfo.getBigImageID());
                ds.writeByte(imageInfo.getX());
                ds.writeByte(imageInfo.getY());
                ds.writeByte(imageInfo.getW());
                ds.writeByte(imageInfo.getH());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getImageData() ", e);
        }
    }

    public void getMapItemType() {
        try {
            System.out.println("get map item type");
            List<MapItemType> mapItemTypes = GameData.getInstance().getMapItemTypes();
            Message ms = new Message(Cmd.MAP_ITEM_TYPE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(mapItemTypes.size());
            for (MapItemType mapItemType : mapItemTypes) {
                ds.writeShort(mapItemType.getId());
                ds.writeUTF(mapItemType.getName());
                ds.writeUTF(mapItemType.getDes());
                ds.writeShort(mapItemType.getImgID());
                ds.writeShort(mapItemType.getIconID());
                ds.writeByte(mapItemType.getDx());
                ds.writeByte(mapItemType.getDy());
                ds.writeShort(mapItemType.getPriceXu());
                ds.writeShort(mapItemType.getPriceLuong());
                ds.writeByte(mapItemType.getBuy());
                List<Position> positions = mapItemType.getListNotTrans();
                ds.writeByte(positions.size());
                for (Position p : positions) {
                    ds.writeByte(p.getX());
                    ds.writeByte(p.getY());
                }
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getMapItemType() ", e);
        }
    }

    public void getTileMap() {
        try {
            byte[] dat = Avatar.getFile(session.getResourcesPath() + "house/tile.png");
            if (dat == null) {
                return;
            }
            Message ms = new Message(Cmd.GET_TILE_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeShort(21);
            ds.writeInt(dat.length);
            ds.write(dat);
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getTileMap() ", e);
        }
    }

    public void getMapItem() {
        try {
            System.out.println("get map item");
            List<MapItem> mapItems = GameData.getInstance().getMapItems();
            Message ms = new Message(Cmd.MAP_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeShort(mapItems.size());
            for (MapItem mapItem : mapItems) {
                ds.writeShort(mapItem.getId());
                ds.writeShort(mapItem.getTypeID());
                ds.writeByte(mapItem.getType());
                ds.writeByte(mapItem.getX());
                ds.writeByte(mapItem.getY());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getMapItem() ", e);
        }
    }

    public void getMapItems(Message ms) {
        try {
            byte[] dat = Avatar.getFile("res/data/map_item.dat");
            ms = new Message(-41);
            DataOutputStream ds = ms.writer();
            ds.write(dat);
            ds.flush();
            sendMessage(ms);
        } catch (EOFException eof) {
            eof.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getMapItemTypes(Message ms) {
        try {
            byte[] dat = Avatar.getFile("res/data/map_item_type.dat");
            ms = new Message(-40);
            DataOutputStream ds = ms.writer();
            ds.write(dat);
            ds.flush();
            sendMessage(ms);
        } catch (EOFException eof) {
            eof.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getBigImage(Message ms) {
        try {
            short id = ms.reader().readShort();
            String folder = session.getResourcesPath() + "big/";
            byte[] dat = Avatar.getFile(folder + id + ".png");
            if (dat == null) {
                return;
            }
            ms = new Message(Cmd.GET_BIG);
            DataOutputStream ds = ms.writer();
            ds.writeShort(id);
            ds.writeShort(dat.length);
            ds.writeShort(dat.length);
            ds.write(dat);
            if (id > 20) {
                ds.writeShort(2);
            } else if (id > 10) {
                ds.writeShort(1);
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getBigImage() ", e);
        }
    }

    public void getBigData() {
        try {
            Message ms = new Message(Cmd.SET_BIG);
            DataOutputStream ds = ms.writer();
            File file = new File(session.getResourcesPath() + "big/");
            File[] listFiles = file.listFiles();
            ds.writeByte(listFiles.length);
            for (File f : listFiles) {
                String name = f.getName().split("\\.")[0];
                int id = Integer.parseInt(name);
                int size = (int) f.length();
                ds.writeShort(id);
                ds.writeShort(size);
            }
            ds.writeShort(ServerManager.bigImgVersion);
            ds.writeShort(ServerManager.partVersion);
            ds.writeShort(ServerManager.bigItemImgVersion);
            ds.writeShort(ServerManager.itemTypeVersion);
            ds.writeShort(ServerManager.itemVersion);
            ds.writeByte(0);
            ds.writeInt(ServerManager.objectVersion);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("getBigData() ", ex);
        }
    }

    public void updateMoney(int type) {
        try {
            Message ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(session.user.xeng);
            ds.writeByte((byte) type);
            ds.writeInt(Math.toIntExact(session.user.xu));
            ds.writeInt(session.user.luong);
            ds.writeInt(session.user.luongKhoa);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("updateMoney ", ex);
        }
    }

    public void openMenuOption(int userID, int menuID, String... menus) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.length);
            for (String menu : menus) {
                ds.writeUTF(menu);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void openMenuOption(int userID, int menuID, List<Menu> menus) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.size());
            for (Menu menu : menus) {
                ds.writeUTF(menu.getName());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void openUIMenu(int userID, int menuID, List<Menu> menus, String npcName, String npcChat) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.size());
            for (Menu m : menus) {
                ds.writeUTF(m.getName());
            }
            for (Menu m : menus) {
                ds.writeShort(m.getId());
            }
            if (npcName != null) {
                ds.writeUTF(npcName);
                ds.writeUTF(npcChat);
                for (Menu m : menus) {
                    ds.writeBoolean(m.isMenu());
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void requestYourInfo(User us) {
        try {
            Message ms = new Message(-22);
            DataOutputStream ds = ms.writer();
            ds.writeInt(us.getId());
            ds.writeByte(us.getLeverMain());
            ds.writeByte(us.getLeverMainPercen());
            ds.writeByte(us.getFriendly());
            ds.writeByte(0); //us.getCrazy()
            ds.writeByte(us.getStylish());
            ds.writeByte(us.getHappy());
            ds.writeByte(100 - us.getHunger());


            if(us.getIdUsHenHo()!=0){
                ds.writeInt(us.getIdUsHenHo());
            }else {
                ds.writeInt(-1);
                ds.writeShort(us.getLeverMain());
                ds.flush();
                sendMessage(ms);
                return;
            }

            //User us2 = UserManager.getInstance().find(1);
            ds.writeUTF(us.getNamehh());
            ds.writeByte(us.getWearingMarry().size());
            for (Item item : us.getWearingMarry()) {
                ds.writeShort(item.getId());
            }

            ds.writeUTF(us.getTenNhan()); // Slogan
            ds.writeShort(us.getImginfo()); // idImage
            ds.writeByte(us.getLevelMarry()); // Level of avatar3
            ds.writeByte(us.getPerLevelMarry()); // Percent level of avatar3
            ds.writeUTF("text 2"); // Relationship
            ds.writeShort(1); // num23
            ds.writeUTF("text 3"); // Action name if num23 != -1

            ds.writeShort(us.getLeverMain());
            ds.flush();

            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getFoodData() {
        try {
            Message ms = new Message(Cmd.GET_ITEM_INFO);
            DataOutputStream ds = ms.writer();
            List<Food> foods = FoodManager.getInstance().getFoods();
            ds.writeShort(foods.size());
            for (Food food : foods) {
                ds.writeShort(food.getId());
                ds.writeUTF(food.getName());
                ds.writeUTF(food.getDescription());
                ds.writeInt(food.getPrice());
                ds.writeByte(food.getShop());
                ds.writeShort(food.getIcon());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getFoodData ", e);
        }
    }

    public void customTab(String title, String content) {
        try {
            Message ms = new Message(Cmd.CUSTOM_TAB);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(title);
            ds.writeUTF(content);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("customTab ", ex);
        }
    }

    public void sellFish(User us,int idFIsh) throws IOException {
        Item item = us.findItemInChests(idFIsh);
        if (item != null && item.getQuantity() > 0) {
            int sell = item.getPart().getCoin();//*item.getQuantity()
            String message = String.format("Bạn vừa bán %d %s với giá = %d xu.", item.getQuantity(), item.getPart().getName(),item.getPart().getCoin(),item.getQuantity(), sell);
            us.removeItem(item.getId(), item.getQuantity());
            us.updateXu(+sell);
            us.getAvatarService().updateMoney(0);
            us.getAvatarService().SendTabmsg(message);
        }
    }


    public void sendEffectStyle4(byte id, byte loopLimit, short num, byte timeStop) {
        try {
            Message ms = new Message(Cmd.EFFECT_OBJ);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(id);
            ds.writeByte(4);
            ds.writeByte(loopLimit);
            ds.writeShort(num);
            ds.writeByte(timeStop);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("send eff ", ex);
        }
    }

    public void sendEffectData(Message mss) {
        try {
            byte id = mss.reader().readByte();
            String folder = session.getResourcesPath() + "effect/";
            byte[] imageData = Avatar.getFile(folder + id + ".png");
            byte[] effData = Avatar.getFile("res/data/effect/" + id + ".dat");


            Message ms = new Message(Cmd.EFFECT_OBJ);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(id);
            ds.writeShort(imageData.length);
            ds.write(imageData);
            ds.write(effData);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public void AdminChest(Message ms) throws IOException {

        List<Item> lstChest = this.session.user.chests;

        List<Item> lstChestHome = Part.shopByPart(PartManager.getInstance().getParts());

        ms = new Message(Cmd.CUSTOM_CHEST);
        DataOutputStream ds = ms.writer();
        ds.writeShort(lstChest.size());
        for (Item item : lstChest) {
            if(item.getId() != 40){
                ds.writeShort(item.getId());
                ds.writeByte(0);
                ds.writeUTF("");
            }
        }
        ds.writeInt(0);
        ds.writeByte(1);

        ds.writeShort(lstChestHome.size());
        for (Item item : lstChestHome) {
            ds.writeShort(item.getId());
            ds.writeByte(0);
            ds.writeUTF("");
        }

        ds.flush();
        this.session.sendMessage(ms); // Gửi thông điệp tới client
    }
    
    public void HandlerMENU_ROTATE(User us, Message mss) {
        try {
            short id = mss.reader().readShort();
            System.out.println("Menu_rotate "+id);
            Message ms = new Message(Cmd.REQUEST_YOUR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeShort(id);
            switch (id) {
//                case 1: {
//                    us.getAvatarService().openMenuOption(1000, 2,
//                            "Hủy hẹn hò ? : không",
//                            "Hủy hẹn hò ? : Có");
//                    break;
//                }
                case 4: {
                    mss = new Message(Cmd.MENU_ROTATE);
                    DataOutputStream ds1 = mss.writer();
                    short num73 = 3;
                    ds1.writeShort(3);
                    int newMoney = 0;
                    ds1.writeInt(newMoney);
                    byte typeBuy = 0;
                    ds1.writeByte(typeBuy);
                    if (num73 != -1)
                    {
                        newMoney = 1;
                        ds1.writeInt(newMoney);
                        typeBuy = 0;
                        ds1.writeByte(typeBuy);
                    }
                    String text5 = "text";
                    ds1.writeUTF(text5);
                    int xu3 = 1;
                    ds1.writeInt(xu3);
                    int luong3 = 2;
                    ds1.writeInt(luong3);
                    int luongKhoa = 3;
                    ds1.writeInt(luongKhoa);
                    ds1.flush();
                    this.session.sendMessage(mss);
                    break;
                }
                //hẹn hò
                case 36: {
                    us.getAvatarService().sendTextBoxPopup(us.getId(), 100, "gửi lời mới hẹn hò tới ? (ghi tên nhân vật)", 0);
                    break;
                }
                case 48: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 1)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 6)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 6)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 47: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 8)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 6)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 8: {
                    long currentTime = System.currentTimeMillis();
                    long lastActionTime = lastActionTimes.getOrDefault(us.getId(), 0L);

                    if (currentTime - lastActionTime < ACTION_COOLDOWN_MS) {
                        us.getAvatarService().serverDialog("Từ từ thôi bạn!");
                        return;
                    }
                    // Cập nhật thời gian thực hiện hành động
                    lastActionTimes.put(us.getId(), currentTime);
                    if (us.getLuong() < 5) {
                        us.getAvatarService().serverDialog("Bạn phải có trên 5 Lượng");
                        return;
                    }
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 16)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 6)
                                .idPlayer(us.getId())
                                .send();
                    });


                    us.updateTopPhaoLuong(-5);
                    us.getAvatarService().updateMoney(0);
                    break;
                }
                case 35: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 46)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 5)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 33: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 48)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 5)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 34: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 45)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 5)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 50: { // đào khoáng sản
                    if(us.getZone().getMap().getId() == 33) {
                        us.getZone().getPlayers().forEach(u -> {
                            EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 22)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 5)
                                .idPlayer(us.getId())
                                .send();
                        });
                        int[] list = {2133, 2134, 2135, 2136, 3672}; // Danh sách khoáng sản
                        Random random = new Random();
                        if (random.nextBoolean()) { // 50% tỉ lệ nhận mảnh ghép, 50% nhận Xu
                        // Chọn ngẫu nhiên một ID từ danh sách trên
                        int idKhoangSan = list[random.nextInt(list.length)];

                        // Chọn số lượng ngẫu nhiên từ 1 đến 3
                        int soLuong = random.nextInt(3) + 1;
                        
                        Item cuoc = us.findItemInChests(2188); // Kiểm tra cuốc
                        if(cuoc == null || cuoc.getQuantity() < 1) {
                            us.getAvatarService().serverDialog("Bạn không có cuốc");
                        }
                        else {
                            // Kiểm tra xem người chơi đã có vật phẩm này trong rương chưa
                            Item KhoangSan = us.findItemInChests(idKhoangSan);
                            if (KhoangSan != null) {
                                KhoangSan.setQuantity(KhoangSan.getQuantity() + soLuong);
                            } else {
                                us.addItemToChests(new Item(idKhoangSan, -1, soLuong));
                            }
                            us.removeItem(2188, 1);
                            Item item = new Item(idKhoangSan, -1, -0);
                            us.getAvatarService().serverDialog("Bạn nhận được " + soLuong + " "+item.getPart().getName());
                        }
                        
                        } else {
                            Item cuoc = us.findItemInChests(2188); // Kiểm tra cuốc
                        if(cuoc == null || cuoc.getQuantity() < 1) {
                            us.getAvatarService().serverDialog("Bạn không có cuốc");
                        }
                        else {
                            us.removeItem(2188, 1); 
                            // đá thổ tỉ lệ 1%
                            if (new Random().nextInt(100) < 1) {
                                Item datho = us.findItemInChests(5004);
                                if(datho != null) {
                                    int quantity = datho.getQuantity();
                                datho.setQuantity(quantity+1);                
                                }
                            else {
                                us.addItemToChests(new Item(5004, -1, 1));
                            }
                                us.getAvatarService().chatTo("admin","Bạn nhận được 1 đá thổ",-1);
                            }
                        }
                        }
                        
                    }
                    else {
                       us.getAvatarService().serverDialog("Bạn chỉ có thể đào khoáng sản ở khu Ai Cập"); 
                    }
                    break;
                }
                case 51: {
                    us.getAvatarService().AdminChest(ms);
                }
                case 9: {
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 11)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 5)
                                .idPlayer(us.getId())
                                .send();
                    });
                    break;
                }
                case 10: {
                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
                        dos1.writeInt(0);//x
                        dos1.flush();
                        byte[] data1 = baos1.toByteArray();
                        MessageHandler msgHandler = new MessageHandler(us.session);
                        msgHandler.onMessage(new Message(Cmd.CONTAINER, data1));
                    }
                    break;
                }
                case 11: {
                    long currentTime = System.currentTimeMillis();
                    long lastActionTime = lastActionTimes.getOrDefault(us.getId(), 0L);

                    if (currentTime - lastActionTime < ACTION_COOLDOWN_MS) {
                        us.getAvatarService().serverDialog("Từ từ thôi bạn!");
                        return;
                    }
                    // Cập nhật thời gian thực hiện hành động
                    lastActionTimes.put(us.getId(), currentTime);
                    if (us.getXu() < 20000) {
                        us.getAvatarService().serverDialog("Bạn phải có trên 20.000 Xu");
                        return;
                    }
                    us.getZone().getPlayers().forEach(u -> {
                        EffectService.createEffect()
                                .session(u.session)
                                .id((byte) 20)
                                .style((byte) 0)
                                .loopLimit((byte) 6)
                                .loop((short) 1)//so luong lap lai
                                .loopType((byte) 1)
                                .radius((short) 6)
                                .idPlayer(us.getId())
                                .send();
                    });
                    us.updateTopPhaoXu(-20000);
                    us.getAvatarService().updateMoney(0);
                    break;
                }
                case 23:{
                    List<Menu> ListDacBiet = new ArrayList<>();
                    ListDacBiet.add(Menu.builder().name("skill mặc định").action(() -> {
                        us.setUseSkill(0);
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Siêu Anh Hùng").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(1)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(1);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải mặc trên 3 món có dame của các set siêu anh hùng");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Cung").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(2)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(2);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01001");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Thú Cưỡi").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(3)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(3);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01002");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Máy Bay").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(4)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(4);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01003");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Thiêu Đốt").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(5)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(5);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01004");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Băng").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(6)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(6);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01005");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("skill Hô Phong Hoán Vũ").action(() -> {
                        if (us.getListSkill() != null && us.getListSkill().contains(7)) {
                            us.getAvatarService().serverDialog("Đổi thành công");
                            us.setUseSkill(7);
                        } else {
                            us.getAvatarService().serverDialog("Bạn phải sử dụng demo 01005");
                        }
                    }).build());
                    ListDacBiet.add(Menu.builder().name("Thoát").id(0).build());
                    us.setMenus(ListDacBiet);
                    us.getAvatarService().openMenuOption(0, 0, ListDacBiet);
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}