package avatar.network;

import avatar.constants.Cmd;
import avatar.constants.NpcName;
import avatar.db.DbManager;
import avatar.handler.*;
import avatar.item.Item;
import avatar.item.PartManager;
import avatar.item.Part;
import avatar.lucky.DialLucky;
import avatar.lucky.DialLuckyManager;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;

import avatar.message.*;
import avatar.model.*;
import avatar.play.*;
import avatar.play.Map;
import avatar.service.*;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.io.IOException;

import avatar.server.Avatar;
import avatar.server.ServerManager;
import avatar.play.offline.AbsMapOffline;
import avatar.play.offline.MapOfflineManager;
import avatar.server.UserManager;
import avatar.server.Utils;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import avatar.play.Map;
import avatar.play.MapManager;
import avatar.play.NpcManager;

public class Session implements ISession {

    private static final byte[] key = (System.currentTimeMillis() + "_avatar").getBytes();
    public Socket sc;
    public DataInputStream dis;
    public DataOutputStream dos;
    public int id;
    public String ip;
    public User user;
    public GlobalHandler handler;
    private IMessageHandler messageHandler;
    public boolean connected;
    public boolean login;
    private byte curR;
    private byte curW;
    private final Sender sender;
    private Thread collectorThread;
    protected Thread sendThread;
    public final Object obj;
    protected String platform;
    protected String versionARM;
    private byte resourceType;
    @Getter
    private AvatarService avatarService;
    @Getter
    private FarmService farmService;
    @Getter
    private HomeService homeService;
    @Getter
    private ParkService parkService;
    @Getter
    private Service service;

    private UpgradeItemHandler upgradeHandler;


    private static final java.util.Map<Integer, Long> lastActionTimes = new HashMap<>();
    private static final long ACTION_COOLDOWN_MS = 100; //

    public Session(Socket sc, int id) throws IOException {
        this.obj = new Object();
        this.resourceType = 0;
        this.sc = sc;
        this.id = id;
        this.ip = ((InetSocketAddress) this.sc.getRemoteSocketAddress()).getAddress().toString().replace("/", "");
        this.dis = new DataInputStream(sc.getInputStream());
        this.dos = new DataOutputStream(sc.getOutputStream());
        this.setHandler(new MessageHandler(this));
        this.sendThread = new Thread(this.sender = new Sender());
        (this.collectorThread = new Thread(new MessageCollector())).start();
        this.avatarService = new AvatarService(this);
        this.farmService = new FarmService(this);
        this.homeService = new HomeService(this);
        this.parkService = new ParkService(this);
        this.service = new Service(this);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void sendMessage(Message message) {
        this.sender.AddMessage(message);
    }

    protected synchronized void doSendMessage(Message m) {
        byte[] data = m.getData();
        try {
            if (this.connected) {
                byte b = this.writeKey(m.getCommand());
                this.dos.writeByte(b);
            } else {
                this.dos.writeByte(m.getCommand());
            }
            if (data != null) {
                int size = data.length;
                if (m.getCommand() == 90) {
                    this.dos.writeInt(size);
                    this.dos.write(data);
                } else {
                    if (this.connected) {
                        int byte1 = this.writeKey((byte) (size >> 8));
                        this.dos.writeByte(byte1);
                        int byte2 = this.writeKey((byte) (size & 0xFF));
                        this.dos.writeByte(byte2);
                    } else {
                        this.dos.writeShort(size);
                    }
                    if (this.connected) {
                        for (int i = 0; i < data.length; ++i) {
                            data[i] = this.writeKey(data[i]);
                        }
                    }
                    this.dos.write(data);
                }
            } else {
                this.dos.writeShort(0);
            }
            this.dos.flush();
            m.cleanup();
        } catch (IOException ex) {
        }
    }

    private byte readKey(byte b) {
        byte[] key = Session.key;
        byte curR = this.curR;
        this.curR = (byte) (curR + 1);
        byte i = (byte) ((key[curR] & 0xFF) ^ (b & 0xFF));
        if (this.curR >= Session.key.length) {
            this.curR %= (byte) Session.key.length;
        }
        return i;
    }

    private byte writeKey(byte b) {
        byte[] key = Session.key;
        byte curW = this.curW;
        this.curW = (byte) (curW + 1);
        byte i = (byte) ((key[curW] & 0xFF) ^ (b & 0xFF));
        if (this.curW >= Session.key.length) {
            this.curW %= (byte) Session.key.length;
        }
        return i;
    }

    @Override
    public void close() {
        try {
            if (this.user != null) {
                this.user.close();
                UserManager.getInstance().remove(user);
            }
            ServerManager.disconnect(this);
            this.cleanNetwork();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanNetwork() {
        this.curR = 0;
        this.curW = 0;
        try {
            this.connected = false;
            this.login = false;
            this.dis.close();
        } catch (Exception ex) {
            try {
                this.dos.close();
            } catch (Exception ex2) {
                try {
                    this.sc.close();
                } catch (Exception ex3) {
                } finally {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                }
            } finally {
                try {
                    this.sc.close();
                } catch (Exception ex4) {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                } finally {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                }
            }
        } finally {
            try {
                this.dos.close();
            } catch (Exception ex5) {
                try {
                    this.sc.close();
                } catch (Exception ex6) {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                } finally {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                }
            } finally {
                try {
                    this.sc.close();
                } catch (Exception ex7) {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                } finally {
                    this.sendThread = null;
                    this.collectorThread = null;
                    System.gc();
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.user != null) {
            return this.user.toString();
        }
        return "Client " + this.id;
    }

    public boolean isResourceHD() {
        return this.resourceType == 1;
    }

    public String getResourcesPath() {
        return isResourceHD() ? ServerManager.resHDPath : ServerManager.resMediumPath;
    }

    public void handshakeMessage() throws IOException {
        Message ms = new Message(-27);
        DataOutputStream ds = ms.writer();
        ds.writeByte(Session.key.length);
        ds.writeByte(Session.key[0]);
        for (int i = 1; i < Session.key.length; ++i) {
            ds.writeByte(Session.key[i] ^ Session.key[i - 1]);
        }
        ds.flush();
        this.doSendMessage(ms);
        this.connected = true;
        this.sendThread.start();
    }

    public void getHandler(Message ms) throws IOException {
        byte index = ms.reader().readByte();
        System.out.println("getHandler: " + index);

        if (index == 8) {
            Zone zone = user.getZone();
            zone.leave(user);
        }
        ms = new Message(Cmd.GET_HANDLER);
        DataOutputStream ds2 = ms.writer();
        ds2.writeByte(index);
        ds2.flush();
        this.sendMessage(ms);
        switch (index) {
            case 3:
                setHandler(new CasinoMsgHandler(this));
                break;
            case 8: {
                setHandler(new AvatarMsgHandler(this));
                break;
            }
            case 9: {
                setHandler(new ParkMsgHandler(this));
                break;
            }
            case 10: {
                setHandler(new FarmMsgHandler(this));
                break;
            }
            case 11: {
                this.setHandler(new HomeMsgHandler(this));
                break;
            }
            case 12: {
                this.setHandler(new RaceMsgHandler(this));
                break;
            }
            default: {
                setHandler(new MessageHandler(this));
                break;
            }
        }
    }

    public void doGetImgIcon(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = this.getResourcesPath() + "object/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.GET_IMG_ICON);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.write(dat);
        ds.flush();
        this.sendMessage(ms);

    }

    // -98 cmd
    public void requestImagePart(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = getResourcesPath() + "item/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.REQUEST_IMAGE_PART);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.write(dat);
        ds.flush();
        this.sendMessage(ms);
    }

    public void doRequestExpicePet(Message ms) throws IOException {
        int userID = ms.reader().readInt();
        ms = new Message(-70);
        DataOutputStream ds = ms.writer();
        ds.writeInt(userID);
        ds.writeByte(0);
        ds.flush();
        this.sendMessage(ms);
    }

    public void clientInfo(Message ms) throws IOException {
        byte provider = ms.reader().readByte();
//        if(provider!=9) {
//            Utils.writeLog(this.user,"login infoFail : provider = "+provider);
//            this.user.session.getAvatarService().serverDialog("Kết nối thất bại. Xin kiểm tra kết nối wifi/3g");
//            this.user.session.close();
//        }
        int memory = ms.reader().readInt();
        String platform = ms.reader().readUTF();
        this.platform = platform;
        int rmsSize = ms.reader().readInt();
        int width = ms.reader().readInt();
        int height = ms.reader().readInt();
        boolean aaaaa = ms.reader().readBoolean();
        byte resource = ms.reader().readByte();
        this.resourceType = resource;
        String version = ms.reader().readUTF();
        if (ms.reader().available() > 0) {
            ms.reader().readUTF();
            ms.reader().readUTF();
            ms.reader().readUTF();
        }
    }

    public void agentInfo(Message ms) throws IOException {
        String agent = ms.reader().readUTF();
        System.out.println("agentInfo: " + agent);
    }


    public  void doRequestService(Message ms) throws IOException {
        byte id = ms.reader().readByte();
        //String msg = ms.reader().readUTF();
        switch (id) {
            case 0:{
                ms = new Message(Cmd.UPDATE_CONTAINER);
                DataOutputStream ds = ms.writer();
                String content = this.user.getUpgradeRequirements();
                ds.writeByte(0);
                ds.writeUTF(content);
                ds.flush();
                this.sendMessage(ms);
                break;
            }
            case 1:{
                ms = new Message(Cmd.UPDATE_CONTAINER);
                DataOutputStream ds = ms.writer();
                String content = this.user.upgradeChest();
                ds.writeByte(1);
                ds.writeUTF(content);
                ds.flush();
                this.sendMessage(ms);
                break;
            }
            case 6: {
                ms = new Message(-10);
                DataOutputStream ds = ms.writer();
                ds.writeUTF(String.format("Bạn đang đăng nhập vào thành phố %s. Dân số %d  người.", ServerManager.cityName, ServerManager.clients.size()));
                ds.flush();
                this.sendMessage(ms);
                break;
            }
            case 3: {
                ms = new Message(-10);
                DataOutputStream ds = ms.writer();
                ds.writeUTF("Chưa có game khác bạn ơiiiii !");
                ds.flush();
                this.sendMessage(ms);
                break;
            }
        }
    }

    public void doLogin(Message ms) throws IOException {
        if (this.login) {
            return;
        }
        String username = ms.reader().readUTF().trim();
        String password = ms.reader().readUTF().trim();
        String version = ms.reader().readUTF().trim();
        this.versionARM = version;
        User us = new User();
        us.setUsername(username);
        us.setPassword(password);
        us.setSession(this);
        boolean result = us.login();
        if (result) {
            this.login = true;
            this.user = us;
            enter();
        } else {
            this.login = false;
        }
    }

    private boolean isCharCreatedPopup;

    private void enter() throws IOException {
        if (user.loadData()) {
            DbManager.getInstance().executeUpdate("UPDATE `players` SET `is_online` = ?, `client_id` = ? , `ip_address` = ? WHERE `user_id` = ? LIMIT 1;", 1, this.id,this.ip, user.getId());
            user.initAvatar();
            this.handler = new GlobalHandler(user);
            UserManager.getInstance().add(user);
            getAvatarService().onLoginSuccess();
            //getAvatarService().serverDialog("Chào mừng bạn đã đến với Thành phố Tuổi Thơ");
            //getAvatarService().serverInfo("");
            //getAvatarService().serverInfo("");
            getAvatarService().serverInfo("Thành phố Play");
        } else {
            if (isCharCreatedPopup) {
                getAvatarService().serverDialog("Có lỗi xảy ra! Vui lòng kiểm tra lại");
                close();
                return;
            }
            isCharCreatedPopup = true;
            DbManager.getInstance().executeUpdate(
                    "INSERT INTO `players`(`user_id`, `level_main`, `gender`, `scores`, `chests`, `wearing`, `chests_home`) VALUES (?, ?, ?, ?, ?, ?, ?);",
                    user.getId(), 1, 0, 0, "[]", "[]", "[]"
            );
            enter();
        }
    }

    public boolean isNewVersion() {
        return true;
    }

    public void regMessage(Message ms) throws IOException {
        String username = ms.reader().readUTF().trim();
        String password = ms.reader().readUTF().trim();
    }

    public void createCharacter(Message ms) throws IOException {
        byte gender = ms.reader().readByte();//1 nam 2 nu
        byte numItem = ms.reader().readByte();
        ArrayList<Item> items = new ArrayList<>();
        short[] boyItems = { 89, 88, 0, 4, 14};
        short[] girlItems = { 89, 88, 0, 4, 49 };
        short[] selectedItems = (gender == 1) ? boyItems : girlItems;

        for (short itemID : selectedItems) {
            items.add(new Item(itemID, -1, 1));
        }
        boolean isError = false;
        if (gender != 1 && gender != 2) {
            isError = true;
        }
        isError = !CreateChar.getInstance().check(gender, items);
        if (isError) {
            ms = new Message(-35);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(false);
            ds.flush();
            this.sendMessage(ms);
            return;
        }
        user.setGender(gender);
        user.setWearing(items);
        ms = new Message(-35);
        DataOutputStream ds = ms.writer();
        ds.writeBoolean(true);
        ds.flush();
        this.sendMessage(ms);
    }

    public void doiKhuVuc(Message ms) throws IOException {
        if (this.messageHandler instanceof FarmMsgHandler) {
            return;
        }
        byte numKhuVuc = 10;
        byte mapid = ms.reader().readByte();
        Map m = MapManager.getInstance().find(mapid);
        ms = new Message(60);
        DataOutputStream ds = ms.writer();
        ds.writeByte(numKhuVuc);
        for (Zone zone : m.getZones()) {
            if (zone.getPlayers().size() >= 9) {
                ds.writeByte(0);
            } else if (zone.getPlayers().size() >= 4) {
                ds.writeByte(1);
            } else {
                ds.writeByte(2);
            }
        }
        ds.flush();
        this.sendMessage(ms);

        ds.close();
    }

    public void doJoinHouse4(Message ms) throws IOException {
        System.out.println("-104:  " + ms.reader().readInt());
    }

    public void buyItemShop(Message ms) {
        try {
            if(this.user.checkFullSlotChest()) {
                return;
            }

            short partID = ms.reader().readShort();
            byte type = ms.reader().readByte();
            if (type < 1 || type > 2) {
                this.user.getService().serverDialog("Có lỗi xảy ra, vui lòng liên hệ admin. Mã lỗi: buyItemShopWrongType");
                return;
            }
            Part part = PartManager.getInstance().findPartByID(partID);

            Item itembyacc = user.findItemInChests(partID);
            if(itembyacc!=null && (itembyacc.getPart().getZOrder() == 30 ||itembyacc.getPart().getZOrder() == 40 ))// mắt mặt ko mua trùng
            {
                user.getAvatarService().serverDialog("bạn đã có vật phẩm này ở rương đồ! đến npc saitama ở công viên để quản lý");
                return;
            }
//            if (((part.getGender() == 2 || part.getGender() == 1) && (user.getGender() != part.getGender())))
//            {
//                user.getAvatarService().serverDialog("Giới tính không phù hợp");
//                return;
//            }
            if (part.getName() == null){
                user.getAvatarService().serverDialog("Không tìm thấy dữ liệu vật phẩm");
                return;
            }
            if (part != null) {
                int priceXu = part.getCoin();
                int priceLuong = part.getGold();
                int price = 0;
                if ((priceXu == -1 && priceLuong == -1) || (type == 1 && priceXu == -1)
                        || (type == 2 && priceLuong == -1)) {
                    return;
                }
                if (priceXu > 0) {
                    price = priceXu;
                    if (user.getXu() < price) {
                        this.user.getService().serverMessage("Bạn không đủ xu!");
                        return;
                    }
                    this.user.updateXu(-price);
                    this.getAvatarService().updateMoney(0);
                } else {
                    price = priceLuong;
                    if (user.getLuong() < price) {
                        this.user.getService().serverMessage("Bạn không đủ lượng!");
                        return;
                    }
                    this.user.updateLuong(-price);
                    this.getAvatarService().updateMoney(0);
                }
                long expired = System.currentTimeMillis() + ((long) part.getExpiredDay() * 86400000L);
                if (part.getExpiredDay() == 0) {
                    expired = -1;
                }
                Item item = Item.builder()
                        .id(part.getId())
                        .expired(expired)
                        .build();
                System.out.println("expired: " + expired);

                // Kiểm tra điều kiện và mặc lên người
                int zOrder = part.getZOrder();
                Item w = user.findItemWearingByZOrder(zOrder);
                if (w != null) {
                    user.removeItemFromWearing(w);
                    user.addItemToChests(w);
                }
                user.addItemToWearing(item);
                user.removeItemFromChests(item);
                user.sortWearing();
                user.getMapService().usingPart(id, (short) item.getId());

                ms = new Message(-24);
                DataOutputStream ds = ms.writer();
                ds.writeShort(partID);
                if (partID != -1) {
                    ds.writeInt(price);
                    ds.writeByte(1);
                }
                ds.writeUTF("Bạn đã mua vật phẩm thành công.");
                ds.writeInt(Math.toIntExact(user.getXu()));
                ds.writeInt(user.getLuong());
                ds.writeInt(user.getLuongKhoa());
                ds.flush();
                this.sendMessage(ms);
            } else {
                this.avatarService.serverMessage("Vật phẩm không tồn tại !!!");
            }
        } catch (Exception e) {
            System.out.println("[ERROR-DB]" + e.getMessage());
        }
    }
    public void doJoinOfflineMap(Message ms) throws IOException {
        byte map = ms.reader().readByte();
        AbsMapOffline mapOffline = MapOfflineManager.getInstance().find(map);
        List<Npc> npcs = new ArrayList<>();
        if (mapOffline != null) {
            npcs = mapOffline.getNpcs();
            System.out.println("Map offline join: " + map);
        } else {
            System.out.println("Map offline join: " + map);
        }
        ms = new Message(Cmd.JOIN_OFFLINE_MAP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(map);
        ds.writeByte(npcs.size());
        for (Npc npc : npcs) {
            ds.writeInt(npc.getId());
            ds.writeUTF(npc.getUsername());
            List<Item> wearing = npc.getWearing();
            ds.writeByte(wearing.size());
            for (Item item : wearing) {
                ds.writeShort(item.getId());
            }
            ds.writeShort(npc.getX());
            ds.writeShort(npc.getY());
            ds.writeByte(npc.getStar());
            ds.writeByte(0);
            ds.writeShort(npc.getIdImg());
            List<String> chats = npc.getTextChats();
            ds.writeByte(chats.size());
            for (String text : chats) {
                ds.writeUTF(text);
            }
        }
        ds.writeShort(0);
        ds.flush();
        this.sendMessage(ms);
    }

    public void doRequestCityMap(Message ms) throws IOException {
        if (ms.reader().available() > 0) {
            byte idMini = ms.reader().readByte();
            System.out.println("RequestCityMap: " + idMini);
        }
        ms = new Message(-63);
        DataOutputStream ds = ms.writer();
        ds.writeByte(-1);
        ds.flush();
        this.sendMessage(ms);
        user.getAvatarService().openMenuOption(5, 0, "Đảo Hawaii", "Ai Cập", "Vương Quốc Bóng Đêm");
    }

    public void doCommunicate(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        int THUONG_NHAN = Npc.ID_ADD + 1211;
        int XENG = Npc.ID_ADD + 1212;
        int THU_NUOI = Npc.ID_ADD + 1213;
        int DOOM = Npc.ID_ADD + 1214;
        int THI_TRUONG = Npc.ID_ADD + 1215;
        if (userId == THUONG_NHAN) {
            List<Menu> thuongnhan = new ArrayList<>();
                Menu shop = Menu.builder().name("Premium").action(() ->{
                List<Item> items = Part.shopByPart(PartManager.getInstance().getShopPremium());
                    if (items == null) {
                        System.out.println("null item thuong nhan");
                        return; // Handle the null case
                    }
                this.user.getAvatarService().openUIShop(5, "Cao cấp", items);
                }).build();
                thuongnhan.add(shop);
                thuongnhan.add(Menu.builder().name("Hướng dẫn").action(() -> {
                    this.user.getAvatarService().customTab("Hướng dẫn", "Hãy chọn trang phục yêu thích của bạn");
                }).build());
                thuongnhan.add(Menu.builder().name("Thoát").build());
                this.user.setMenus(thuongnhan);
                this.user.getAvatarService().openMenuOption(THUONG_NHAN, 0, thuongnhan);
        }
        else if (userId == XENG) {
            List<Menu> xeng = new ArrayList<>();
                Menu shop = Menu.builder().name("Shop Xèng").action(() ->{
                List<Item> items = Part.shopByPart(PartManager.getInstance().getShopXeng());
                    if (items == null) {
                        System.out.println("null item xeng");
                        return; // Handle the null case
                    }
                this.user.getAvatarService().openUIShop(5, "Xèng", items);
                }).build();
                xeng.add(shop);
                xeng.add(Menu.builder().name("Hướng dẫn").action(() -> {
                    this.user.getAvatarService().customTab("Hướng dẫn", "Hãy chọn trang phục yêu thích của bạn");
                }).build());
                xeng.add(Menu.builder().name("Thoát").build());
                this.user.setMenus(xeng);
                this.user.getAvatarService().openMenuOption(XENG, 0, xeng);
        }
        else if (userId == THU_NUOI) {
            List<Menu> thucung = new ArrayList<>();
                Menu shop = Menu.builder().name("Shop thú nuôi").action(() ->{
                List<Item> shoppet = Part.shopByPart(PartManager.getInstance().getShopPet());
                    if (shoppet == null) {
                        System.out.println("Không tìm thấy thú nuôi");
                        return; // Handle the null case
                        }
                        this.user.getAvatarService().openUIShop(5, "Thú nuôi", shoppet);
                    }).build();
                    thucung.add(shop);
                    thucung.add(Menu.builder().name("Hướng dẫn").action(() -> {
                        this.user.getAvatarService().customTab("Hướng dẫn", "Hãy chọn vật nuôi yêu thích của bạn");
                    }).build());
                    thucung.add(Menu.builder().name("Thoát").build());
                    this.user.setMenus(thucung);
                    this.user.getAvatarService().openUIMenu(userId, 0, thucung, "", "Hãy chọn vật nuôi yêu thích của bạn");
        }
        else if (userId == DOOM) {
            ShopTradeHandler.displayUI(this.user,0, 1214,3361,3455,3456,3457,5100,5158,5159,2043,2054,3112,4482,4059,3565,2898,4541,4539,4525,4540,4527,4374,4298,4297,4280,4279,4274,4153,6837);
        }
        else if (userId == THI_TRUONG) {
            List<Menu> thitruong = new ArrayList<>();
            thitruong.add(Menu.builder().name("Nhiệm vụ").action(() -> {
                this.user.getAvatarService().customTab("Nhiệm vụ", "Hiện tại chưa có nhiệm vụ");
            }).build());
            thitruong.add(Menu.builder().name("Thoát").build());
            this.user.setMenus(thitruong);
            this.user.getAvatarService().openMenuOption(THI_TRUONG, 0, thitruong);
        }
        else if (userId >= 2000000000 && userId != THU_NUOI && userId != THUONG_NHAN && userId != XENG && userId != DOOM && userId != THI_TRUONG) {
            System.out.println("doCommunicate:" + userId);
            NpcHandler.handlerCommunicate(userId, this.user);
        }
        else {
            System.out.println("userId = " + userId);
            if(userId == 2) {
                this.user.getAvatarService().serverDialog("Bảng xếp hạng câu cá");
            }
            // BXH nông trại
            if(userId == 3) {
                List<Menu> bxhfarm = new ArrayList<>();
                bxhfarm.add(Menu.builder().name("Top cao thủ level")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopLv();
                            this.user.getAvatarService().BxhLv(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Top cao thủ lượt đánh boss")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopdameboss();
                            this.user.getAvatarService().BxhenterBoss(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Top Sử dụng xu")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopuseCoin();
                            this.user.getAvatarService().BxhuseCoin(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Top Sử dụng lượng")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopuseGold();
                            this.user.getAvatarService().BxhuseGold(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Top đại gia Xu")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopMoney();
                            this.user.getAvatarService().BxhMoney(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Top đại gia Lượng")
                        .action(() -> {
                            List<User> topPlayers = this.user.getService().getTopMoneygold();
                            this.user.getAvatarService().BxhMoneyGold(topPlayers);
                        })
                        .build());
                bxhfarm.add(Menu.builder().name("Thoát").build());
                this.user.setMenus(bxhfarm);
                this.user.getAvatarService().openMenuOption(NpcName.TOP, 0, bxhfarm);
                    this.user.setMenus(bxhfarm);
                    this.user.getAvatarService().openMenuOption(3, 0, bxhfarm);
            }
            if(userId == 4) {
                this.user.getAvatarService().serverDialog("Câu cá");
            }
            if (userId == 0) {
                // hiện thị menu chức năng
                List<Menu> menus = new ArrayList<>(List.of(
                        Menu.builder().name("Auto Câu Cá").menus(
                                        List.of(
                                                Menu.builder().name("Kích hoạt Auto Câu Cá").action(() -> {
//                                                    String checkNap = "SELECT ThuongNapLanDau FROM users WHERE id = ? LIMIT 1;";
//                                                    try (Connection connection = DbManager.getInstance().getConnection();
//                                                         PreparedStatement ps = connection.prepareStatement(checkNap);) {
//                                                        ps.setInt(1, user.getId());
//                                                        ResultSet rs = ps.executeQuery();
//                                                        while (rs.next()) {
//                                                            boolean napLanDau = rs.getBoolean("ThuongNapLanDau");
//                                                            if(napLanDau){
//                                                                this.user.getAvatarService().serverDialog("Bạn Đã Kích Hoạt Auto Câu Cá Thành Công");
//                                                                this.user.setAutoFish(true);
//                                                            }
//                                                            else {
//                                                                this.user.getAvatarService().serverDialog("Treo câu thì donate lần đầu nha : v");
//                                                                this.user.setAutoFish(false);
//                                                            }
                                                            this.user.setAutoFish(true);
                                                        }
                                                        //rs.close();
                                                    //} catch (SQLException ex) {
                                                    //    Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                                                   // }
                                                ).build(),
                                                Menu.builder().name("Tắt Auto Câu Cá").action(() -> {
                                                    this.user.getAvatarService().serverDialog("Bạn Đã Tắt Auto Câu Cá");
                                                    this.user.setAutoFish(false);
                                                }).build()
                                        ))
                                .build(),
                            Menu.builder().name("Xóa vật phẩm ngày").action(()-> {
                            // Lấy slot của người dùng
                            int slot = user.getChestSlot();

                            if (slot > 0) {
                            // Lấy danh sách vật phẩm trong rương
                                List<Item> items = user.getChests();

                                // Kiểm tra nếu danh sách vật phẩm không rỗng
                                if (items != null && !items.isEmpty()) {
                                // Duyệt qua các vật phẩm trong rương từ cuối lên đầu
                                    for (int i = items.size() - 1; i >= 0; i--) {
                                        Item item = items.get(i);

                                            // Kiểm tra hạn sử dụng của vật phẩm
                                            String expired = item.expiredString().trim();

                                            // Nếu vật phẩm có hạn sử dụng hợp lệ (không phải chuỗi trống)
                                            if (!expired.isEmpty()) {
                                                // Xóa vật phẩm
                                                user.removeItem(item.getId(), 1);
                                                System.out.println("Xoa vat pham ID: " + item.getId() + " o vi tri: " + i);
                                            } else {
                                                // Log nếu vật phẩm không có hạn sử dụng (chuỗi trống)
                                                System.out.println("Khong xoa vat pham ID: " + item.getId() + " han su dung vinh vien o vi : " + i);
                                            }
                                    }

                                // Thông báo đã xóa thành công
                                user.getAvatarService().serverDialog("Xóa thành công");
                            } else {
                                // Nếu không có vật phẩm trong rương
                                user.getAvatarService().serverDialog("Không có vật phẩm để xóa.");
                            }
                            } else {
                                // Nếu không có slot hợp lệ
                                user.getAvatarService().serverDialog("Không có rương hoặc slot không hợp lệ.");
                            }
                        }).build(),

                        Menu.builder().name("Giftcode").action(() -> {
                            user.getAvatarService().sendTextBoxPopup(user.getId(), 20, "Giftcode:", 1);
                        }).build()
                ));
                if (user.getRole() == 9) {
                    menus.add(0, Menu.builder().name("Quản lý Admin")
                            .menus(List.of(
                                    Menu.builder().name("Gửi Lượng").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 5, "Nhập số lượng - khoảng cách - tài khoản", 1);
                                    }).build(),
                                    Menu.builder().name("Gửi Xu").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 6, "Nhập số xu - khoảng cách - tài khoản", 1);
                                    }).build(),
                                    Menu.builder().name("Gửi vật phẩm").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 7, "Nhập ID vật phẩm - khoảng cách - tài khoản", 1);
                                    }).build(),
                                    Menu.builder().name("Chat tổng").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 8, "Nội dung", 1);
                                    }).build(),
                                    Menu.builder().name("Thời Tiết").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 9, "thoi tiet", 1);
                                    }).build(),
                                    Menu.builder().name("Bảo trì").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 98, "Bảo trì", 1);
                                    }).build(),
                                    Menu.builder().name("infor").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 11, "infor", 1);
                                    }).build(),
                                    Menu.builder().name("thread?").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 12, "thread", 1);
                                    }).build(),
                                    Menu.builder().name("pem").action(() -> {
                                            user.getZone().getPlayers().forEach(u -> {
                                                EffectService.createEffect()
                                                        .session(u.session)
                                                        .id((byte) 23)
                                                        .style((byte) 0)
                                                        .loopLimit((byte) 5)
                                                        .loop((short) 1)
                                                        .loopType((byte) 1)
                                                        .radius((short) 5)
                                                        .idPlayer(user.getId())
                                                        .send();
                                            });
                                    }).build(),
                                    Menu.builder().name("Tim Rơi").action(() -> {
                                            user.getZone().getPlayers().forEach(u -> {
                                                EffectService.createEffect()
                                                        .session(u.session)
                                                        .id((byte) 56)
                                                        .style((byte) 0)
                                                        .loopLimit((byte) 5)
                                                        .loop((short) 100)
                                                        .loopType((byte) 1)
                                                        .radius((short) 250)
                                                        .idPlayer(user.getId())
                                                        .send();
                                            });
                                    }).build(),
                                    Menu.builder().name("Menu sentb bao tri").action(() -> {
                                        user.getAvatarService().sendTextBoxPopup(user.getId(), 98, "bao tri sau 2p", 1);
                                    }).build(),
                                    Menu.builder().name("EFFECT").action(() -> {
                                        if(user.getId() == 7){

                                            user.getAvatarService().sendTextBoxPopup(user.getId(), 99, "ideffect", 1);

                                        }else{
                                            user.getAvatarService().serverDialog("ad mới bật được b ơi");
                                        }
                                    }).build(),
                                    Menu.builder().name("Khoá nick").build()
                            ))
                            .build());
                }
                user.setMenus(menus);
                user.getAvatarService().openUIMenu(1, 0, menus, null, null);

            }
//            handleSelectFunction(this.user);

        }
    }


//Nâng cấp các shop
    public void handleBossShop(Message ms) throws IOException {
        int idBoss = ms.reader().readInt();
        byte type = ms.reader().readByte();
        short indexItem = ms.reader().readShort();
        if (idBoss == Npc.ID_ADD + NpcName.THO_KIM_HOAN && user.getBossShopItems() != null) {
            System.out.println(MessageFormat.format("do upgrade item boss shop {0}, {1}, {2},"
                    , idBoss, type, indexItem));
            UpgradeItem upgradeItem = (UpgradeItem) user.getBossShopItems().get(indexItem);
            if (upgradeItem != null) {
                Item item = user.findItemInChests(upgradeItem.getItemNeed());
                if (item == null) {
                    Part part = PartManager.getInstance().findPartById(upgradeItem.getItemNeed());
                    getService().serverDialog(MessageFormat.format("Bạn cần có {0} để nâng cấp món đồ này", part.getName()));
                    return;
                }
                if (type == BossShopHandler.SELECT_XU) {
                    if (upgradeItem.isOnlyLuong()) {
                        getService().serverDialog("Vật phẩm này chỉ có thể nâng cấp bằng lượng");
                        return;
                    }
                    if (user.getXu() < upgradeItem.getXu()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} xu để nâng cấp món đồ này", upgradeItem.getXu()));
                        return;
                    }
                    user.updateXu(-upgradeItem.getXu());
                    user.getAvatarService().updateMoney(0);
                    Utils.writeLog(this.user,"xu Nâng Cấp Item " +upgradeItem.getItem().getPart().getName() + " " + this.user.getXu());
                    doFinalUpgrade(upgradeItem, item);
                    return;
                } else if (type == BossShopHandler.SELECT_LUONG) {
                    if (user.getLuong() < upgradeItem.getLuong()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} lượng để nâng cấp món đồ này", upgradeItem.getLuong()));
                        return;
                    }
                    user.updateLuong(-upgradeItem.getLuong());
                    user.getAvatarService().updateMoney(0);
                    Utils.writeLog(this.user,"Luong Nâng Cấp Item " +upgradeItem.getItem().getPart().getName()+ " " +this.user.getLuong());
                    doFinalUpgrade(upgradeItem, item);
                    return;
                } else if (type == BossShopHandler.SELECT_DNS) {
                    Item item1 = user.findItemInChests(3672);
                    if (item1 == null || item1.getQuantity() < upgradeItem.getScores()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} Đá ngũ sắc để nâng cấp món đồ này", upgradeItem.getScores()));
                        return;
                    }

                    if (user.getLuong() < upgradeItem.getLuong()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} lượng để nâng cấp món đồ này", upgradeItem.getLuong()));
                        return;
                    }

                    if (user.getXu() < upgradeItem.getXu()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} xu để nâng cấp món đồ này", upgradeItem.getXu()));
                        return;
                    }
                    user.removeItem(3672,upgradeItem.getScores());
                    user.updateLuong(-upgradeItem.getLuong());
                    user.getAvatarService().updateMoney(0);
                    user.updateXu(-upgradeItem.getXu());
                    user.getAvatarService().updateMoney(0);
                    Utils.writeLog(this.user,"Xu Luong Nâng Cấp Item " +upgradeItem.getItem().getPart().getName() + this.user.getXu()+" luong " + this.user.getLuong());
                    doFinalUpgrade(upgradeItem, item);
                    return;
                }else if (type == BossShopHandler.SELECT_HoaNS) {
                    Item item1 = user.findItemInChests(5389);
                    int Quanty = upgradeItem.getScores();
                    if(Quanty == 12){
                        Quanty = 20;
                    }
                    if (item1 == null || item1.getQuantity() < Quanty) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} Sen Ngũ Sắc để nâng cấp món đồ này", Quanty));
                        return;
                    }

                    if (user.getLuong() < upgradeItem.getLuong()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} lượng để nâng cấp món đồ này", upgradeItem.getLuong()));
                        return;
                    }

                    if (user.getXu() < upgradeItem.getXu()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} xu để nâng cấp món đồ này", upgradeItem.getXu()));
                        return;
                    }
                    user.removeItem(5389,Quanty);
                    user.updateLuong(-upgradeItem.getLuong());
                    user.getAvatarService().updateMoney(0);
                    user.updateXu(-upgradeItem.getXu());
                    user.getAvatarService().updateMoney(0);
                    Utils.writeLog(this.user,"Xu Luong Nâng Cấp Item " +upgradeItem.getItem().getPart().getName() + this.user.getXu()+" luong " + this.user.getLuong());
                    doFinalUpgrade(upgradeItem, item);
                    return;
                }
                else if (type == BossShopHandler.SELECT_ManhGhep) {
                    Item ManhGhep = user.findItemInChests(upgradeItem.getItemNeed());
                    if (ManhGhep == null || ManhGhep.getQuantity() < upgradeItem.getScores()) {
                        getService().serverDialog(MessageFormat.format("Bạn cần có {0} Mảnh ghép để đổi {1} ", upgradeItem.getScores(),upgradeItem.getItem().getPart().getName()));
                        return;
                    }
                    user.removeItem(upgradeItem.getItemNeed(),upgradeItem.getScores());
                    upgradeItem.getItem().setExpired(-1);
                    user.addItemToChests(upgradeItem.getItem());
                    getService().serverDialog(MessageFormat.format("Chúc mừng bạn đã đổi thành công {0}", upgradeItem.getItem().getPart().getName()));
                    return;
                }
            }
        }
        else if(idBoss == Npc.ID_ADD + NpcName.TIEN_CA && user.getBossShopItems() != null) {
            TradeItem tradeItem = (TradeItem) user.getBossShopItems().get(indexItem);
            int items = tradeItem.getItemRequest();
            if(items == 3122) {
                Item mvht1 = user.findItemInChests(3119);
                Item mvht2 = user.findItemInChests(3120);
                Item mvht3 = user.findItemInChests(3121);
                if(mvht1 == null || mvht2 == null || mvht3 == null) {
                    user.getAvatarService().serverDialog("Bạn cần 3 mảnh vỡ huyền thoại 1 + 2 + 3 để ghép Huy hiệu huyền thoại");
                }
                else {
                    user.removeItem(3119, 1);
                    user.removeItem(3120, 1);
                    user.removeItem(3121, 1);
                    if(this.user.findItemInChests(tradeItem.getItem().getId()) !=null){
                        tradeItem.getItem().setExpired(-1);
                        int quantity = this.user.findItemInChests(tradeItem.getItem().getId()).getQuantity();
                        this.user.findItemInChests(tradeItem.getItem().getId()).setQuantity(quantity+1);
                    }else {
                        tradeItem.getItem().setExpired(-1);
                        tradeItem.getItem().setQuantity(1);
                        this.user.addItemToChests(tradeItem.getItem());
                    }
                    user.getAvatarService().serverDialog("Bạn nhận được Huy hiệu huyền thoại vĩnh viễn");
                }
            }
            else if(items == 3505) {
                Item mvhg1 = user.findItemInChests(3501);
                Item mvhg2 = user.findItemInChests(3502);
                Item mvhg3 = user.findItemInChests(3503);
                Item mvhg4 = user.findItemInChests(3504);
                if(mvhg1 == null || mvhg2 == null || mvhg3 == null || mvhg4 == null) {
                    user.getAvatarService().serverDialog("Bạn cần 4 mảnh vỡ hoàng gia 1 + 2 + 3 + 4 để ghép Huy hiệu hoàng gia");
                }
                else {
                    user.removeItem(3501, 1);
                    user.removeItem(3502, 1);
                    user.removeItem(3503, 1);
                    user.removeItem(3504, 1);
                    if(this.user.findItemInChests(tradeItem.getItem().getId()) !=null){
                        tradeItem.getItem().setExpired(-1);
                        int quantity = this.user.findItemInChests(tradeItem.getItem().getId()).getQuantity();
                        this.user.findItemInChests(tradeItem.getItem().getId()).setQuantity(quantity+1);
                    }else {
                        tradeItem.getItem().setExpired(-1);
                        tradeItem.getItem().setQuantity(1);
                        this.user.addItemToChests(tradeItem.getItem());
                    }
                    user.getAvatarService().serverDialog("Bạn nhận được Huy hiệu hoàng gia vĩnh viễn");

                }
            }
            else {
                user.getAvatarService().serverDialog("Không tìm thấy dữ liệu vật phẩm");
            }
            System.out.println(tradeItem.getItemRequest());
        }
        else if(idBoss == Npc.ID_ADD + 1214 && user.getBossShopItems() != null||idBoss == Npc.ID_ADD + NpcName.PHU_THUY_NGUYEN_TO && type == 1  && user.getBossShopItems() != null) {
            TradeItem tradeItem = (TradeItem) user.getBossShopItems().get(indexItem);
            for (TradeItemNeed need : tradeItem.itemNeeds) {
                Item item = user.findItemInChests(need.id);
                if (item == null || item.getQuantity() < need.quantity) {
                    getService().serverDialog(MessageFormat.format(
                            "Bạn cần có {0} {1} để đổi {2}",
                            need.quantity,
                            PartManager.getInstance().findPartById(need.id).getName(),
                            tradeItem.getItem().getPart().getName()
                    ));
                    return;
                }
            }
            if (user.getXu() < tradeItem.getCoin()) {
                getService().serverDialog(MessageFormat.format(
                        "Bạn cần có {0} xu để đổi {1}",
                        tradeItem.getCoin(),
                        tradeItem.getItem().getPart().getName()
                ));
                return;
            }
            if (user.getLuong() < tradeItem.getGold()) {
                getService().serverDialog(MessageFormat.format(
                        "Bạn cần có {0} lượng để đổi {1}",
                        tradeItem.getGold(),
                        tradeItem.getItem().getPart().getName()
                ));
                return;
            }
            for (TradeItemNeed need : tradeItem.itemNeeds) {
                user.removeItem(need.id, need.quantity);
            }
            user.updateXu(tradeItem.getCoin());
            user.updateLuong(tradeItem.getGold());
            tradeItem.getItem().setExpired(-1);
            user.addItemToChests(tradeItem.getItem());
            getService().serverDialog(MessageFormat.format("Chúc mừng bạn đã đổi thành công {0}", tradeItem.getItem().getPart().getName()));
            return;
        }
//        else if(idBoss == Npc.ID_ADD + NpcName.PHU_THUY_NGUYEN_TO && type == 1  && user.getBossShopItems() != null) {
//            TradeItem tradeItem = (TradeItem) user.getBossShopItems().get(indexItem);
//            Item canhngusac = user.findItemInChests(4736);
//            Item nguyenlieu = user.findItemInChests(tradeItem.getItemneed());
//            if(canhngusac == null) {
//                getService().serverDialog("Bạn cần có cánh ngũ sắc");
//                return;
//            }
//            if (nguyenlieu == null || nguyenlieu.getQuantity() < tradeItem.getPoint()) {
//                getService().serverDialog(MessageFormat.format("Bạn cần có {0} {1}", tradeItem.getPoint(),PartManager.getInstance().findPartById(tradeItem.getItemneed()).getName()));
//                return;
//            }
//            if(user.getLuong() < 500) {
//                getService().serverDialog("Bạn không đủ 500 lượng");
//                return;
//            }
//            user.removeItem(4736,1);
//            user.removeItem(tradeItem.getItemneed(),tradeItem.getPoint());
//            tradeItem.getItem().setExpired(-1);
//            user.addItemToChests(tradeItem.getItem());
//            user.updateLuong(-500);
//            user.getAvatarService().updateMoney(1);
//            getService().serverDialog(MessageFormat.format("Chúc mừng bạn đã đổi thành công {0}", tradeItem.getItem().getPart().getName()));
//            return;
//        }
//        else if(idBoss == Npc.ID_ADD + NpcName.PHU_THUY_NGUYEN_TO && type == 2  && user.getBossShopItems() != null) {
//            TradeItem tradeItem = (TradeItem) user.getBossShopItems().get(indexItem);
//            Item haoquangngusac = user.findItemInChests(5355);
//            Item nguyenlieu = user.findItemInChests(tradeItem.getItemneed());
//            if(haoquangngusac == null) {
//                getService().serverDialog("Bạn cần có hào quang ngũ sắc");
//                return;
//            }
//            if (nguyenlieu == null || nguyenlieu.getQuantity() < tradeItem.getPoint()) {
//                getService().serverDialog(MessageFormat.format("Bạn cần có {0} {1}", tradeItem.getPoint(),PartManager.getInstance().findPartById(tradeItem.getItemneed()).getName()));
//                return;
//            }
//            if(user.getLuong() < 500) {
//                getService().serverDialog("Bạn không đủ 500 lượng");
//                return;
//            }
//            user.removeItem(5355,1);
//            user.removeItem(tradeItem.getItemneed(),tradeItem.getPoint());
//            tradeItem.getItem().setExpired(-1);
//            user.addItemToChests(tradeItem.getItem());
//            user.updateLuong(-500);
//            user.getAvatarService().updateMoney(1);
//            getService().serverDialog(MessageFormat.format("Chúc mừng bạn đã đổi thành công {0}", tradeItem.getItem().getPart().getName()));
//            return;
//        }
         else if(idBoss == Npc.ID_ADD + NpcName.PHI_HANH_GIA && user.getBossShopItems() != null) {
            ShopNpc shopNpc = (ShopNpc) user.getBossShopItems().get(indexItem);
            if (shopNpc.getXuneed() <= 0) {
                if(user.getLuong() < shopNpc.getLuongneed()) {
                    getService().serverDialog(MessageFormat.format("Bạn không đủ {0} lượng để mua {1} ",shopNpc.getLuongneed(),shopNpc.getItem().getPart().getName()));
                }
                else {
                    if(shopNpc.getItem().getPart().getType() == -2){
                    if(this.user.findItemInChests(shopNpc.getItem().getId()) !=null){
                            shopNpc.getItem().setExpired(-1);
                            int quantity = this.user.findItemInChests(shopNpc.getItem().getId()).getQuantity();
                            this.user.findItemInChests(shopNpc.getItem().getId()).setQuantity(quantity+1);
                        }else {
                            shopNpc.getItem().setExpired(-1);
                            this.user.addItemToChests(shopNpc.getItem());
                        }
                    user.updateLuong(-shopNpc.getLuongneed());
                    getAvatarService().requestYourInfo(user);
                    getAvatarService().updateMoney(0);
                    getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                }
                    else
                    {
                       shopNpc.getItem().setExpired(-1);
                       this.user.addItemToChests(shopNpc.getItem());
                       user.updateLuong(-shopNpc.getLuongneed());
                        getAvatarService().requestYourInfo(user);
                        getAvatarService().updateMoney(0);
                        getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                    }
                }
                return;
            }
            else if(shopNpc.getLuongneed() <= 0) {
            if(user.getXu() < shopNpc.getXuneed()) {
                    getService().serverDialog(MessageFormat.format("Bạn không đủ {0} xu để mua {1} ",shopNpc.getLuongneed(),shopNpc.getItem().getPart().getName()));
                }
                else {
                if(shopNpc.getItem().getPart().getType() == -2){
                    if(this.user.findItemInChests(shopNpc.getItem().getId()) !=null){
                            shopNpc.getItem().setExpired(-1);
                            int quantity = this.user.findItemInChests(shopNpc.getItem().getId()).getQuantity();
                            this.user.findItemInChests(shopNpc.getItem().getId()).setQuantity(quantity+1);
                        }else {
                            shopNpc.getItem().setExpired(-1);
                            this.user.addItemToChests(shopNpc.getItem());
                        }
                    user.updateXu(-shopNpc.getXuneed());
                    getAvatarService().requestYourInfo(user);
                    getAvatarService().updateMoney(1);
                    getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                }
                else {
                    shopNpc.getItem().setExpired(-1);
                    this.user.addItemToChests(shopNpc.getItem());
                     user.updateXu(-shopNpc.getXuneed());
                    getAvatarService().requestYourInfo(user);
                    getAvatarService().updateMoney(1);
                    getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                }
            }
                return;
            }
            else {
                if(user.getXu() < shopNpc.getXuneed()) {
                    getService().serverDialog(MessageFormat.format("Bạn không đủ {0} xu để mua {1} ",shopNpc.getLuongneed(),shopNpc.getItem().getPart().getName()));
                }
                else if(user.getLuong() < shopNpc.getLuongneed()) {
                    getService().serverDialog(MessageFormat.format("Bạn không đủ {0} lượng để mua {1} ",shopNpc.getLuongneed(),shopNpc.getItem().getPart().getName()));
                }
                else {
                    if(shopNpc.getItem().getPart().getType() == -2){
                    if(this.user.findItemInChests(shopNpc.getItem().getId()) !=null){
                            shopNpc.getItem().setExpired(-1);
                            int quantity = this.user.findItemInChests(shopNpc.getItem().getId()).getQuantity();
                            this.user.findItemInChests(shopNpc.getItem().getId()).setQuantity(quantity+1);
                        }else {
                            shopNpc.getItem().setExpired(-1);
                            this.user.addItemToChests(shopNpc.getItem());
                        }
                    user.updateXu(-shopNpc.getXuneed());
                    user.updateLuong(-shopNpc.getLuongneed());
                    getAvatarService().requestYourInfo(user);
                    getAvatarService().updateMoney(0);
                    getAvatarService().updateMoney(1);
                    getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                }
                    else
                    {
                        shopNpc.getItem().setExpired(-1);
                        this.user.addItemToChests(shopNpc.getItem());
                        user.updateXu(-shopNpc.getXuneed());
                        user.updateLuong(-shopNpc.getLuongneed());
                        getAvatarService().requestYourInfo(user);
                        getAvatarService().updateMoney(0);
                        getAvatarService().updateMoney(1);
                        getService().serverDialog(MessageFormat.format("Mua thành công {0}. Hãy kiểm tra trong kho hàng",shopNpc.getItem().getPart().getName()));
                    }
                }
                return;
            }
        }
    }

    public boolean isItemExchanged(int userId, int itemId) {
        String query = "SELECT COUNT(*) FROM itemLimited WHERE user_id = ? AND item_id = ?";
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);

            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Đã tồn tại bản ghi, nghĩa là đã đổi
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Chưa đổi
    }


    public void saveItemExchange(int userId, int itemId) {
        String query = "INSERT INTO itemLimited (user_id, item_id) VALUES (?, ?)";
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Đã lưu vật phẩm vào bảng itemLimited.");
            } else {
                System.out.println("Không có dòng nào được thêm vào. Kiểm tra lại điều kiện.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private boolean isGenderCompatible(Item item, User user) {
        int itemGender = item.getPart().getGender(); // Giới tính của item (0 = cả hai giới, 1 = nam, 2 = nữ)
        int userGender = user.getGender(); // Giới tính của user (1 = nam, 2 = nữ)

        // Nếu itemGender là 0, thì cả hai giới đều dùng được
        if (itemGender == 0) {
            return true;
        }

        // Nếu không, kiểm tra xem giới tính của item có khớp với giới tính của user không
        return itemGender == userGender;
    }
    private void doFinalUpgrade(UpgradeItem item, Item itemOld) {

        long currentTime = System.currentTimeMillis();
        long lastActionTime = lastActionTimes.getOrDefault(this.user.getId(), 0L);
        if (currentTime - lastActionTime < ACTION_COOLDOWN_MS) {
            this.user.getAvatarService().serverDialog("Từ từ thôi bạn!");
            return;
        }
        lastActionTimes.put(this.user.getId(), currentTime);
        if(itemOld.getExpired()!=-1){
            user.getAvatarService().serverDialog("Bạn cần có vật phẩm "+itemOld.getPart().getName()+ " vĩnh viễn");
            return;
        }
        int ratio = item.getRatio();
        boolean isUpgradeSuccess = false;
        if (ratio > 0) {
            isUpgradeSuccess = Utils.nextInt(0, 100) < ratio;
        } else {
            ratio = Math.abs(ratio);
            int correctNumber = Utils.nextInt(0, ratio);
            isUpgradeSuccess = correctNumber == Utils.nextInt(0, ratio);
        }
        if (isUpgradeSuccess||item.getRatio() == 100) {
            user.removeItemFromChests(itemOld);
            item.getItem().setExpired(-1);
            user.addItemToChests(item.getItem());
            getAvatarService().updateMoney(0);
            user.setStylish((byte) (user.getStylish() - 1));
            getAvatarService().requestYourInfo(user);
            List<User> players = this.user.getZone().getPlayers();
            for (User player : players) {
                EffectService.createEffect()
                        .session(player.session)
                        .id((byte)16)
                        .style((byte) 0)
                        .loopLimit((byte) 5)
                        .loop((short) 1)
                        .loopType((byte) 1)
                        .radius((short) 1)
                        .idPlayer(NpcName.THO_KIM_HOAN+Npc.ID_ADD)
                        .send();
            player.getMapService().doAvatarFeel(player.getId(), (byte)3);
            player.getMapService().doAvatarFeel(player.getId(), (byte)120);
            };
            getService().serverDialog("Chúc mừng bạn đã ghép đồ thành công");

            Zone z = user.getZone();
            if (z != null) {

                Npc npc = NpcManager.getInstance().find(z.getMap().getId(), z.getId(), NpcName.THO_KIM_HOAN + Npc.ID_ADD);
                if (npc == null) {
                    for (User player : players) {
                        EffectService.createEffect()
                                .session(player.session)
                                .id((byte)16)
                                .style((byte) 0)
                                .loopLimit((byte) 5)
                                .loop((short) 1)
                                .loopType((byte) 1)
                                .radius((short) 1)
                                .idPlayer(NpcName.THO_KIM_HOAN+Npc.ID_ADD)
                                .send();
                player.getMapService().doAvatarFeel(player.getId(), (byte)3);
                player.getMapService().doAvatarFeel(player.getId(), (byte)120);
                    };
                }
                npc.setTextChats(List.of(MessageFormat.format("Chúc mừng bạn {0} đã nâng cấp vật phẩm {1} thành công", user.getUsername(), item.getItem().getPart().getName())));
            } else {
                return;
            }
        } else {
            getAvatarService().updateMoney(0);
            getService().serverDialog("Ghép đồ thất bại. Chúc bạn may mắn lần sau");
        }
    }

    public void doDialLucky(Message ms) throws IOException {
        short partId = ms.reader().readShort();
        short degree = ms.reader().readShort();
        DialLucky dl = user.getDialLucky();
        if (dl != null) {
            if (dl.getType() == DialLuckyManager.MIEN_PHI) {
                Item itm = user.findItemInChests(593);
                if (itm == null || itm.getQuantity() <= 0) {
                    return;
                }
                user.removeItem(593, 1);
            }
            if (dl.getType() == DialLuckyManager.XU) {
                if (user.getXu() < 15000) {
                    return;
                }
                user.updateXu(-25000);
            }
            if (dl.getType() == DialLuckyManager.LUONG) {
                if (user.getLuong() < 5) {
                    return;
                }
                user.updateLuong(-5);
            }
            getAvatarService().updateMoney(0);
            dl.doDial(user, partId, degree);
        }
    }

    public void requestTileMap(Message ms) throws IOException {
        byte idTileImg = ms.reader().readByte();
        System.out.println("map = " + idTileImg);
        byte[] dat = Avatar.getFile(getResourcesPath() + "tilemap/" + idTileImg + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.REQUEST_TILE_MAP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(idTileImg);
        ds.write(dat);
        ds.flush();
        this.sendMessage(ms);
    }
    //tab img
    public void postcustomTabImg() throws IOException {
        try {
            Message msgTab = new Message(-58);
            DataOutputStream ds = msgTab.writer();
            ds.writeByte(1);
            ds.writeShort(15);
            byte[] imageData = Avatar.getFile(this.getResourcesPath() + "farm/45.png");
            if (imageData == null) {
                return;
            }
            ds.writeShort(imageData.length);
            ds.write(imageData);
            ds.writeUTF("Thông báo");
            StringBuilder content = new StringBuilder();
            content.append("µ15,17,0µ").append("Nội dung thông báo\n");
            ds.writeUTF(content.toString());
            ds.writeByte(0);

            ds.flush();
            sendMessage(msgTab);
        } catch (Exception e) {

        }
    }
    
    public void dragonInfo() throws IOException {
        try {
            Message msgTab = new Message(-58);
            DataOutputStream ds = msgTab.writer();
            ds.writeByte(1);
            ds.writeShort(15);
            byte[] imageData = Avatar.getFile(this.getResourcesPath() + "item/13604.png");
            if (imageData == null) {
                return;
            }
            ds.writeShort(imageData.length);
            ds.write(imageData);
            ds.writeUTF("Luyện rồng");
            StringBuilder content = new StringBuilder();
            content.append("µ15,17,0µ").append("Địa long thần\n Tiến độ: 10%\n Yêu cầu: 1 rồng tí nị + 10 đá thổ");
            ds.writeUTF(content.toString());
            ds.writeByte(0);

            ds.flush();
            sendMessage(msgTab);
        } catch (Exception e) {

        }
    }
    
    public void clanInfor() throws IOException {
    try (Connection connection = DbManager.getInstance().getConnection(); 
         PreparedStatement ps = connection.prepareStatement(
             "SELECT c.*, cm.*, (SELECT COUNT(*) FROM clan_members WHERE clan_id = c.id) AS member_count " +
             "FROM clans c " +
             "JOIN clan_members cm ON c.id = cm.clan_id " +
             "WHERE cm.user_id = ?;")) {

        ps.setInt(1, this.user.getId()); // Truyền user_id vào câu lệnh chuẩn bị

        try (ResultSet res = ps.executeQuery()) {
            if (res.next()) { // Nếu có kết quả trả về, người chơi đã vào bang

                String clan_name = res.getString("name");
                String icon = res.getString("icon");
                int xu = res.getInt("xu");
                int luong = res.getInt("luong");
                int member = res.getInt("max_members");
                int currentMemberCount = res.getInt("member_count"); // Đếm số thành viên hiện tại

                // Tạo thông báo với hình ảnh
                try {
                    Message msgTab = new Message(-58);
                    DataOutputStream ds = msgTab.writer();
                    ds.writeByte(1); // Một số byte dùng cho mục đích nào đó
                    ds.writeShort(15); // Một giá trị độ dài hoặc kiểu dữ liệu tùy ý

                    // Đọc dữ liệu hình ảnh và kiểm tra
                    byte[] imageData = Avatar.getFile(this.getResourcesPath() + "object/" + icon + ".png");
                    // Gửi thông tin hình ảnh
                    ds.writeShort(imageData.length); // Gửi độ dài hình ảnh
                    ds.write(imageData); // Gửi dữ liệu hình ảnh
                    ds.writeUTF("Hội nhóm"); // Tiêu đề thông báo
                    StringBuilder content = new StringBuilder();
                    content.append("µ15,17,0µ").append("  Tên bang: " + clan_name + "\nXu: " + xu + "\nLượng: " + luong + 
                                     "\nThành viên: " + currentMemberCount + "/" + member);
                    
                    ds.writeUTF(content.toString());
                    ds.writeByte(0); // Kết thúc
                    ds.flush(); // Đảm bảo tất cả dữ liệu đã được gửi
                    sendMessage(msgTab); // Gửi thông báo đến người chơi
                } catch (Exception e) {
                    // Xử lý lỗi nếu có trong quá trình gửi hình ảnh hoặc tạo thông báo
                    e.printStackTrace();
                }
            } else {
                // Nếu không tìm thấy bang hội của người chơi, in ra thông báo
                System.out.println("Bạn chưa vào bang.");
            }
        }
    } catch (SQLException e) {
        // Xử lý lỗi khi truy vấn cơ sở dữ liệu
        e.printStackTrace();
    }
}

    public void doParkBuyItem(Message ms) throws IOException {
        short id = ms.reader().readShort();
        Food food = FoodManager.getInstance().findFoodByFoodID(id);
        if (food != null) {
            int shop = food.getShop();
            int price = food.getPrice();
            if (price > user.xu) {
                this.user.getService().serverDialog("Bạn không đủ xu!");
                return;
            }
            String name = food.getName();
            this.user.updateXu(-price);
            if (shop == 4) {
                int health = 100 - this.user.getHunger() + food.getPercentHelth();
                health = ((health > 100) ? 100 : health);
                this.user.updateHunger(100 - health);
                this.user.getAvatarService()
                        .serverDialog(String.format("Bạn đã ăn một %s sức khoẻ bạn hiện tại là %d", name, health));
            } else if (shop == 5) {
                this.user.getService().serverDialog("Bạn đã cho thú nuôi ăn thành công");
            }
        }
    }

    public void requestFriendList(Message ms) throws IOException {

        //this.user.getAvatarService().chatTo("admin","ok",-1);
        //this.user.getAvatarService().serverDialog("comingsion");
        //return;
        List<User> friends = null;
        this.user.getAvatarService().listFriend(friends);
    }

    public void joinHouse(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        Vector<HouseItem> hItems = new Vector<>();

        try (Connection connection = DbManager.getInstance().getConnection();) {
            String GET_HOUSE_DATA = "SELECT * FROM `house_buy` WHERE `user_id` = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(GET_HOUSE_DATA);
            ps.setInt(1, userId);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                JSONArray ja_map = (JSONArray) JSONValue.parse(res.getString("map_data"));
                byte[] map_data = new byte[ja_map.size()];
                for (int i = 0; i < ja_map.size(); ++i) {
                    map_data[i] = ((Long) ja_map.get(i)).byteValue();
                }
                ps.close();
                res.close();

                String GET_ITEMS_IN_CHEST = "SELECT * FROM `house_player_item` WHERE `user_id` = ?";
                ps = connection.prepareStatement(GET_ITEMS_IN_CHEST);
                ps.setInt(1, userId);
                res = ps.executeQuery();

                if (res != null) {
                    while (res.next()) {
                        HouseItem hItem = new HouseItem();
                        hItem.itemId = res.getShort("house_item_id");
                        hItem.x = res.getByte("x");
                        hItem.y = res.getByte("y");
                        hItem.rotate = res.getByte("rotate");
                        hItems.add(hItem);
                    }
                }
                ps.close();
                res.close();


                //this.user.getZone().leave(user);
                Item pet = user.findItemWearingByZOrder(-1);
                if(pet == null) {
                    ms = new Message(-65);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(3);
                    ds.writeInt(userId);
                    ds.writeShort(map_data.length);
                    for (int j = 0; j < map_data.length; ++j) {
                        ds.write(map_data[j]);
                    }
                    ds.writeByte(28);
                    ds.writeShort(hItems.size());
                    for (HouseItem hItem2 : hItems) {
                        ds.writeShort(hItem2.itemId);
                        ds.writeByte(hItem2.x);
                        ds.writeByte(hItem2.y);
                        ds.writeByte(hItem2.rotate);
                    }
                    ds.flush();
                    this.sendMessage(ms);
                }
                else {
                  this.user.session.avatarService.serverDialog("Không mang pet vào nhà");  
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void BuyHouse() throws IOException {

        int userId =  this.user.session.user.getId();
        try (Connection connection = DbManager.getInstance().getConnection();) {
            String GET_HOUSE_DATA = "SELECT * FROM `house_buy` WHERE `user_id` = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(GET_HOUSE_DATA);
            ps.setInt(1, userId);
            ResultSet res = ps.executeQuery();

            if (res.next()) {
                this.user.session.avatarService.serverDialog("Bạn đã mua nhà rồi !");
                return;
            }
            if(this.user.session.user.getXu() < 1000000)
            {
                this.user.session.avatarService.serverDialog("Bạn không đủ tiền để mua nhà !");
                return;
            }

            this.user.updateXu(-1000000);
            this.user.getAvatarService().updateMoney(0);
            // Nếu chưa mua, tiến hành chèn dữ liệu nhà mới
            String INSERT_HOUSE_DATA = "INSERT INTO `house_buy` (user_id, type, map_data, date_expired) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertPs = connection.prepareStatement(INSERT_HOUSE_DATA)) {
                insertPs.setInt(1, userId);
                insertPs.setInt(2, 3); // `type`: thay đổi theo logic của bạn
                insertPs.setString(3, "[39,39,39,39,34,35,35,35,35,35,35,34,35,35,35,34,35,35,35,35,35,35,34,39,39,39,39,39,39,39,39,39,38,25,25,25,25,25,25,36,27,27,27,36,25,25,25,25,25,25,37,39,39,39,39,39,39,39,39,39,38,26,26,26,26,26,26,36,28,28,28,36,26,26,26,26,26,26,37,39,39,39,39,39,39,39,39,39,38,14,14,14,14,14,14,36,3,3,3,36,14,14,14,14,14,14,37,39,39,39,39,39,39,39,39,39,38,14,14,14,14,14,14,25,3,3,3,25,14,14,14,14,14,14,37,39,39,39,39,39,39,39,39,39,38,14,14,14,14,14,14,26,3,3,3,26,14,14,14,14,14,14,37,39,39,39,39,39,39,39,39,39,38,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,37,39,39,39,39,39,39,39,39,39,25,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,37,39,39,39,39,39,34,35,35,35,25,35,35,35,35,35,35,38,14,14,14,14,14,14,14,14,14,14,25,39,39,39,39,39,38,27,27,27,36,23,23,23,23,23,23,36,0,0,35,35,35,35,35,35,35,35,25,35,35,35,35,34,38,28,28,28,36,24,24,24,24,24,24,36,0,0,23,23,23,23,23,23,23,23,36,23,23,23,23,36,38,17,17,17,25,9,9,18,9,9,9,25,0,0,24,24,24,24,24,24,24,24,36,24,24,24,24,36,38,17,17,17,26,9,9,9,9,9,9,26,0,0,9,9,9,9,9,9,9,9,36,9,9,9,9,36,38,17,17,17,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,25,9,9,9,9,36,38,17,17,17,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,26,9,9,9,9,36,35,35,35,35,35,35,35,35,35,35,35,38,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,36,23,23,23,23,23,23,23,23,23,23,23,38,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,36,24,24,24,24,24,24,24,24,24,24,24,35,35,35,35,9,9,9,9,35,35,35,35,35,35,35,35,35,39,39,39,39,39,39,39,39,39,39,39,24,24,24,24,40,40,40,40,24,24,24,24,24,24,24,24,24]"); // `map_data`
                insertPs.setString(4, "2000-01-01"); // `date_expired`: bạn có thể thay đổi thành giá trị ngày tháng hợp lệ

                int rowsInserted = insertPs.executeUpdate();
                if (rowsInserted > 0) {
                    this.user.session.avatarService.serverDialog("Bạn đã mua nhà thành công!");


                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void closeMessage() {
        if (this.isConnected()) {
            if (this.messageHandler != null) {
                this.messageHandler.onDisconnected();
            }
            this.close();
        }
    }

    public void changePassword(Message ms) throws IOException {
        String passOld = ms.reader().readUTF();
        String passNew = ms.reader().readUTF();
        try (Connection connection = DbManager.getInstance().getConnection()) {
            String ACCOUNT_LOGIN = "SELECT * FROM `users` WHERE `id` = ? AND `password` = ? LIMIT 1";
            PreparedStatement ps = connection.prepareStatement(ACCOUNT_LOGIN);
            ps.setInt(1, this.user.getId());
            ps.setString(2, Utils.md5(passOld));
            ResultSet red = ps.executeQuery();
            if (red.next()) {
                String ACCOUNT_UPDATE_PASSWORD = "UPDATE `users` SET `password` = ? WHERE `id` = ?";
                PreparedStatement changePass = connection.prepareStatement(ACCOUNT_UPDATE_PASSWORD);
                changePass.setString(1, Utils.md5(passNew));
                changePass.setInt(2, this.user.getId());
                int result = changePass.executeUpdate();
                if (result > 0) {
                    ms = new Message(-62);
                    DataOutputStream ds = ms.writer();
                    ds.writeUTF(passNew);
                    ds.flush();
                    this.sendMessage(ms);
                    this.user.getService().serverDialog("\u0110\u1ed5i m\u1eadt kh\u1ea9u th\u00e0nh c\u00f4ng.");
                } else {
                    this.user.getAvatarService()
                            .serverDialog("C\u00f3 l\u1ed7i x\u1ea3y ra, vui l\u00f2ng th\u1eed l\u1ea1i sau.");
                }
                changePass.close();
            } else {
                this.user.getService().serverDialog("M\u1eadt kh\u1ea9u c\u0169 kh\u00f4ng \u0111\u00fang.");
            }
            red.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private class Sender implements Runnable {

        private Deque<Message> sendingMessage;

        public Sender() {
            this.sendingMessage = new ArrayDeque<Message>();
        }

        public void AddMessage(Message message) {
            this.sendingMessage.add(message);
        }

        @Override
        public void run() {
            while (isConnected()) {
                while (!this.sendingMessage.isEmpty()) {
                    Message message = this.sendingMessage.poll();
                    doSendMessage(message);
                }
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    class MessageCollector implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Message message = this.readMessage();
                    if (message == null) {
                        break;
                    }
                    messageHandler.onMessage(message);
                    message.cleanup();
                }
            } catch (Exception ex) {
            }
            if (isConnected()) {
                if (messageHandler != null) {
                    messageHandler.onDisconnected();
                }
                close();
            }
        }

        private Message readMessage() {
            try {
                byte cmd = dis.readByte();
                if (connected) {
                    cmd = readKey(cmd);
                }
                int size;
                if (connected) {
                    byte b1 = dis.readByte();
                    byte b2 = dis.readByte();
                    size = ((readKey(b1) & 0xFF) << 8 | (readKey(b2) & 0xFF));
                } else {
                    size = dis.readUnsignedShort();
                }
                byte[] data = new byte[size];
                for (int len = 0, byteRead = 0; len != -1 && byteRead < size; byteRead += len) {
                    len = dis.read(data, byteRead, size - byteRead);
                    if (len > 0) {
                    }
                }
                if (connected) {
                    for (int i = 0; i < data.length; ++i) {
                        data[i] = readKey(data[i]);
                    }
                }
                Message msg = new Message(cmd, data);
                return msg;
            } catch (Exception e) {
            }
            return null;
        }
    }
}