package avatar.model;

import avatar.constants.Cmd;
import avatar.db.DbManager;
import avatar.item.Item;
import avatar.lib.RandomCollection;
import avatar.message.MessageHandler;
import avatar.message.ParkMsgHandler;
import avatar.network.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import avatar.network.Session;
import avatar.play.MapManager;
import avatar.play.Zone;
import avatar.server.ServerManager;
import avatar.server.UserManager;
import avatar.server.Utils;
import avatar.service.EffectService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static avatar.server.ServerManager.clients;

public class Boss extends User {
    @Getter
    @Setter
    private List<String> textChats;
    private Map<Integer, List<int[]>> zoneCoordinates = new HashMap<>();//tọa độ boss di chuyển trong map
    public Boss() {
        super();
        initializeCoordinates();

        //tọa độ boss dichuyeeren
        List<int[]> map11 = Arrays.asList(
                new int[]{319, 106},
                new int[]{779, 114}
        );
        zoneCoordinates.put(11, map11);
        List<int[]> map7 = Arrays.asList(
                new int[]{212, 90},
                new int[]{112, 118},
                new int[]{292, 118}
        );
        zoneCoordinates.put(7, map7);
        List<int[]> map1 = Arrays.asList(
                new int[]{80, 105},
                new int[]{244, 109},
                new int[]{172, 154}
        );
        zoneCoordinates.put(1, map1);

        autoChatBot();
    }

    private void initializeCoordinates() {
        List<int[]> map11 = Arrays.asList(
                new int[]{270, 100}
        );
        zoneCoordinates.put(11, map11);

        List<int[]> map7 = Arrays.asList(
                new int[]{216, 97}
        );
        zoneCoordinates.put(7, map7);

        List<int[]> map1 = Arrays.asList(
                new int[]{165, 100}
        );
        zoneCoordinates.put(1, map1);

        List<int[]> map2 = Arrays.asList(
                new int[]{204, 85}
        );
        zoneCoordinates.put(2, map2);

        List<int[]> map3 = Arrays.asList(
                new int[]{288, 97}
        );
        zoneCoordinates.put(3, map3);

        List<int[]> map5 = Arrays.asList(
                new int[]{188, 89}
        );
        zoneCoordinates.put(5, map5);

        List<int[]> map8 = Arrays.asList(
                new int[]{264, 69}
        );
        zoneCoordinates.put(8, map8);
    }


    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int TOTAL_BOSSES = 400000000; // Tổng số Boss muốn tạo
    public static int currentBossId = 1001 + Npc.ID_ADD; // ID bắt đầu cho Boss
    private static int bossCount = 0; // Đếm số lượng Boss đã được tạo
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ScheduledExecutorService scheduler1 = Executors.newSingleThreadScheduledExecutor();
    // Tạo cấu trúc dữ liệu để quản lý boss requirements
    private static final Map<Integer, Integer> BOSS_REQUIREMENTS = new HashMap<>() {{
        put(1, 1);
        put(2, 1);
        put(3, 1);
        put(5, 1);
        put(7, 1);
        put(8, 1);
        put(11, 1);
    }};

    private void autoChatBot() {
        Runnable autoChatTask = () -> {
            try {
                if (textChats == null) {
                    textChats = new ArrayList<>(); // Hoặc khởi tạo với một giá trị mặc định
                }

                for (String text : textChats) {
                    getMapService().chat(this, text);
                    Thread.sleep(12000); // Vẫn sleep giữa các câu chat
                }

                if (textChats == null || textChats.size() == 0) {
                    Thread.sleep(10000);
                }

//                int[][] pairs = {
//                        {23, 24},
//                        {25, 26},
//                        {38, 39},
//                };
//                if(!this.isPhatqua()&&this.getHP()>0){
//                    Random rand = new Random();
//                    int randomIndex = rand.nextInt(pairs.length);
//                    int[] selectedPair = pairs[randomIndex];
//                    BossSkillRanDomUser((byte)selectedPair[0], (byte)selectedPair[1]);
//                    int mapId = this.getZone().getMap().getId();
//                    List<int[]> coordinates = zoneCoordinates.get(mapId);
//                    int[] randomCoordinate = coordinates.get(new Random().nextInt(coordinates.size()));
//                    moveBossXY(this, randomCoordinate[0], randomCoordinate[1]);
//                }
                if (this.isPhatqua() && this.getSoqua() > 0) {
                    int mapId = this.getZone().getMap().getId();
                    List<int[]> coordinates = zoneCoordinates.get(mapId);
                    if (coordinates != null && !coordinates.isEmpty()) {
                        int[] randomCoordinate = coordinates.get(new Random().nextInt(coordinates.size()));
                        moveBossXY(this, randomCoordinate[0], randomCoordinate[1]);
                        System.out.println("phat qua map" + mapId + " khu = " + this.getZone().getId());
                        this.updatesoQua(this);
                        Phatqua(this);
                    } else {
                        System.err.println("Không có tọa độ cho bản đồ ID " + mapId);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Đảm bảo xử lý ngắt
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        scheduler1.scheduleWithFixedDelay(autoChatTask, 0, 15, TimeUnit.SECONDS);
    }


//    private Thread autoChatBot = new Thread(() -> {
//        while (true) {
//            try {
//                if (textChats == null) {
//                    textChats = new ArrayList<>(); // Hoặc khởi tạo với một giá trị mặc định
//                }
//                for (String text : textChats) {
//                    getMapService().chat(this, text);
//                    Thread.sleep(12000);
//                }
//                if (textChats == null || textChats.size() == 0) {
//                    Thread.sleep(10000);
//                }
//                int[][] pairs = {
//                        {23, 24},
//                        {25, 26},
//                };
//                if(this.isPhatqua()&& this.getSoqua()>0)//(this.getHP()>0)
//                {
//                    //Random rand = new Random();
//                    //int randomIndex = rand.nextInt(pairs.length);
//                    //int[] selectedPair = pairs[randomIndex];
//                    //BossSkillRanDomUser((byte)selectedPair[0], (byte)selectedPair[1]);
//
//                    // Tạo đối tượng Random để chọn tọa độ ngẫu nhiên
//                    int mapId = this.getZone().getMap().getId();
//                    List<int[]> coordinates = zoneCoordinates.get(mapId);
//                    if (coordinates != null && !coordinates.isEmpty()) {
//                        int[] randomCoordinate = coordinates.get(new Random().nextInt(coordinates.size()));
//                        moveBossXY(this, randomCoordinate[0], randomCoordinate[1]);
//                        System.out.println("phat qua map"+mapId+"khu = "+this.getZone().getId());
//                        this.updatesoQua(this);
//                        Phatqua(this);
//                    } else {
//                        // Nếu không có tọa độ nào, có thể xử lý trường hợp này ở đây
//                        System.err.println("Không có tọa độ cho bản đồ ID " + mapId);
//                    }
//                }
//            } catch (InterruptedException ignored) {
//                Thread.currentThread().interrupt(); // Đảm bảo xử lý gián đoạn
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    });


    public void Phatqua(Boss boss) {
        Zone zone = boss.getZone();
        if (zone != null) {
            List<User> players = zone.getPlayers();
            if (players != null) {
                for (User player : players) {
                    if (player != null) {  // Kiểm tra player có phải là null không
                        if (player.getHopqua() >= 0 && player.getHopqua() <= 100) {
                            player.updateHopQua(+1);
                            if (player.session != null) {  // Kiểm tra avatarService có phải là null không
                                player.getAvatarService().serverDialog("Bạn nhận được 1 hộp quà từ ông già noel số quà + " + player.getHopqua() + "/100 hộp");
                                addqua(player);
                            } else {
                                System.out.println("AvatarService is null for player " + player.getId());
                            }
                        } else {
                            if (player.getAvatarService() != null) {
                                player.getAvatarService().serverDialog("Bạn đã nhận đủ số quà trong ngày hôm nay rồi");
                            } else {
                                System.out.println("AvatarService is null for player " + player.getId());
                            }
                        }
                    }
                }
            } else {
                System.out.println("Players list is null.");
            }
        } else {
            System.out.println("Zone is null.");
        }
    }


    private void addqua(User us) {
        Item hopqua = new Item(5583,-1,1);
        if(us.findItemInChests(5583) !=null){
            int quantity = us.findItemInChests(5583).getQuantity();
            us.findItemInChests(5583).setQuantity(quantity+1);
        }else {
            us.addItemToChests(hopqua);
            us.updatevatphamcauca(+1);
        }
        //us.getAvatarService().SendTabmsg("Bạn vừa nhận được 1 "+ " " + hopqua.getPart().getName());

    }


    public synchronized void handleBossDefeat(Boss boss, User us) throws IOException {
        // Cộng 100 lượng
        us.updateLuong(100);

        // Log xu nhận được (giữ nguyên, dù không có updateXu)
        System.out.println("Người chơi [" + us.getUsername() + "] nhận được " + us.getStoredXuUpdate() + " xu từ boss!");

//        DbManager.getInstance().executeUpdate("UPDATE `players` SET `xu_from_boss` = ? WHERE `user_id` = ? LIMIT 1;",
//                us.xu_from_boss, us.getId());

        DbManager.getInstance().executeUpdate(
                "UPDATE `players` SET `xu_from_boss` = `xu_from_boss` + 1 WHERE `user_id` = ? LIMIT 1;",
                us.getId()
        );

        System.out.println(" user : "+us.getId() +" được cộng 1 xu_from_boss");
        // Thêm phần thưởng ngẫu nhiên: sen ngũ sắc (ID 5389) hoặc đá ngũ sắc (ID 3672)
        Random random = new Random();
        int chance = random.nextInt(100); // Số ngẫu nhiên từ 0-99
        int newItemId;
        String newItemName;

        // Kiểm tra chỗ trống trong rương
        if (us.chests.size() < us.getChestSlot()) {
            // Xác suất 40% nhận sen ngũ sắc, 60% nhận đá ngũ sắc
            if (chance < 40) {
                newItemId = 5389; // Sen ngũ sắc
                newItemName = "Sen Ngũ Sắc";
            } else {
                newItemId = 3672; // Đá ngũ sắc
                newItemName = "Đá Ngũ Sắc";
            }

            // Thêm item mới vào rương
            Item newItem = new Item(newItemId, -1, 1); // Số lượng 1, vĩnh viễn
            us.addItemToChests(newItem);
            System.out.println(" Người chơi [" + us.getUsername() + "] nhận được 1 " + newItemName);
            us.getAvatarService().SendTabmsg("Bạn vừa tiêu diệt " + boss.getUsername() + ": nhận 100 lượng và 1 " + newItemName + ".");
        } else {
            System.out.println("Người chơi [" + us.getUsername() + "] rương đầy, không nhận được item ngẫu nhiên!");
            us.getAvatarService().SendTabmsg("Bạn vừa tiêu diệt " + boss.getUsername() + ": nhận 100 lượng. Rương đầy, không nhận được item!");
        }



        // Thông báo boss bị tiêu diệt
        String username = us.getUsername();
        String message = String.format("Khá lắm bạn %s đã kill được %s", username, boss.getUsername().substring(3, boss.getUsername().length() - 6));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> newMessages = Arrays.asList(message, "ta sẽ hủy diệt thành phố này");
        this.textChats = new ArrayList<>(newMessages);
        for (String chatMessage : textChats) {
            getMapService().chat(boss, chatMessage);
            textChats.remove(chatMessage);
        }

//        // Xử lý tạo quà xung quanh boss
//        Zone khuqua = boss.getZone();
//        scheduler.schedule(() -> {
//            try {
//                LocalTime now = LocalTime.now();
//                LocalTime tenAM = LocalTime.of(10, 0);
//                LocalTime twoPM = LocalTime.of(14, 0);
//                LocalTime sevenPM = LocalTime.of(17, 0);
//                LocalTime elevenPM = LocalTime.of(23, 0);
//
//                if ((now.isAfter(tenAM) && now.isBefore(twoPM)) || (now.isAfter(sevenPM) && now.isBefore(elevenPM))) {
//                    createNearbyGiftBoxes(boss, khuqua, boss.getX(), boss.getY(), Boss.currentBossId + 10000);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }, 5, TimeUnit.SECONDS);

        // Lấy zone và danh sách người chơi TRƯỚC KHI xóa boss
        Zone currentZone = boss.getZone();

        List<User> players = new ArrayList<>(currentZone.getPlayers());

        // Xóa boss khỏi map ngay lập tức
        boss.getZone().leave(boss);
        boss.session.close();


        // Thông báo cho tất cả người chơi trong khu vực
        players.forEach(player -> {
            if (player != null && player.session != null) {
                player.getAvatarService().serverInfo("Ngưỡng mộ quá.Người chơi " + us.getUsername() + "đã hạ gục boss !!!");
            }
        });

        // Lên lịch hồi sinh boss sau 20 giây
        scheduler.schedule(() -> {
            try {
                // Tìm map thiếu boss
                int mapIdToSpawn = findMapNeedingBoss();

                if (mapIdToSpawn != -1) {
                    // Lấy tọa độ ngẫu nhiên cho map đó
                    List<int[]> coordinates = zoneCoordinates.get(mapIdToSpawn);
                    int[] coordinate = coordinates.isEmpty() ?
                            new int[]{100, 100} :
                            coordinates.get(new Random().nextInt(coordinates.size()));
                    int randomZoneId = ThreadLocalRandom.current().nextInt(0, 10); // [1, 10]
                    spawnBossAt(mapIdToSpawn, randomZoneId, (short)coordinate[0], (short)coordinate[1],
                            Utils.nextInt(500000, 1000000));
                } else {
                    System.out.println("Tất cả map đã có đủ boss!");
                }

                // Thông báo cho tất cả người chơi
                players.forEach(player -> {
                    if (player != null && player.session != null) {
                        player.getAvatarService().serverInfo("Boss đã hồi sinh, hãy săn đuổi nó !!!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20, TimeUnit.MINUTES);

    }

    // Gửi hiệu ứng cho người chơi trong khu vực
    public synchronized void hanlderNhatHopQua(User boss, User us) throws IOException {
        us.getAvatarService().serverDialog("bạn đã nhặt được hộp quà");
        //int time = Utils.getRandomInArray(new int[]{3, 7, 15, 30});
        Item hopqua = new Item(683,-1,1);
        //hopqua.setExpired(System.currentTimeMillis() + (86400000L * time));

        if(us.findItemInChests(683) !=null){
            int quantity = us.findItemInChests(683).getQuantity();
            us.findItemInChests(683).setQuantity(quantity+1);
        }else {
            us.addItemToChests(hopqua);
        }
        ServerManager.disconnect(boss.session);
        boss.session.close();
    }


    public void addBossToZone(User boss,int Map ,Zone zone, short x, short y,int hp) throws IOException {
        if (bossCount >= TOTAL_BOSSES) {
            return; // Dừng nếu đã tạo đủ số lượng Boss
        }

        boss.getWearing().clear();
        boss.setId(currentBossId++);
        boss.setDefeated(false);
//        List<String> chatMessages = Arrays.asList("YAAAA", "YOOOO");
//        ((Boss) boss).setTextChats(chatMessages);
        assignRandomItemToBoss(boss);
        boss.setHP(hp);
        if(boss.getWearing().get(1).getId() == 5112){
//            boss.setHP(hp+90000);
            List<String> chatMessages = Arrays.asList("gãi ngứa hả tên kia", "Mau nộp kẹo cho taaaa");
            ((Boss) boss).setTextChats(chatMessages);
        }
        boss.bossMapId = Map;
        bossCount++; // Tăng số lượng Boss đã tạo
        sendAndHandleMessages(boss);
        moveBoss(boss);
        int mapId = zone.getMap().getId();
///
        List<int[]> coordinates = zoneCoordinates.get(mapId);
        if (coordinates != null && !coordinates.isEmpty()) {
            int[] randomCoordinate = coordinates.get(new Random().nextInt(coordinates.size()));
            moveBossXY(boss, randomCoordinate[0], randomCoordinate[1]);
        } else {
            // Nếu không có tọa độ nào, có thể xử lý trường hợp này ở đây
            System.err.println("Không có tọa độ cho bản đồ ID " + mapId);
        }
    }


    //    private void MoveArea(User boss) throws IOException {
//        ByteArrayOutputStream joinPank = new ByteArrayOutputStream();
//        try (DataOutputStream dos2 = new DataOutputStream(joinPank)) {
//            dos2.writeByte(boss.bossMapId);
//            System.err.println("joinmaopboss " + boss.bossMapId);
//            dos2.writeByte(Utils.nextInt(9));
//            dos2.writeShort(boss.getX());//x
//            dos2.writeShort(boss.getY());//y
//            dos2.flush();
//            byte[] dataJoinPak = joinPank.toByteArray();
//            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
//            parkMsgHandler1.onMessage(new Message(Cmd.AVATAR_JOIN_PARK, dataJoinPak));
//        }
//        System.out.println("add boss khu :" + boss.getZone().getId());
//
//    }
    private void MoveArea(User boss) throws IOException {
        ByteArrayOutputStream joinPank = new ByteArrayOutputStream();
        try (DataOutputStream dos2 = new DataOutputStream(joinPank)) {
            dos2.writeByte(boss.bossMapId);
            System.err.println("joinmaopboss " + boss.bossMapId);
            int randomZoneId = ThreadLocalRandom.current().nextInt(0, 10); // [1, 10]
            dos2.writeByte(randomZoneId); // Thay vì random, fix cứng khu 0

//            dos2.writeByte(0); // Thay vì random, fix cứng khu 0
            dos2.writeShort(boss.getX());//x
            dos2.writeShort(boss.getY());//y
            dos2.flush();
            byte[] dataJoinPak = joinPank.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.AVATAR_JOIN_PARK, dataJoinPak));
        }
        System.out.println("add boss khu :" + boss.getZone().getId());
    }

    private User createBoss(short x, short y,int id) {
        User boss = new Boss();
        boss.setId(id);
        boss.setX(x);
        boss.setY(y);
        return boss;
    }

    private void createGiftBox(Zone zone, short x, short y, int giftId) throws IOException {
        User giftBox = createBoss(x, y, giftId);
        assignGiftItemToBoss(giftBox);// Gán item cho hộp quà
        giftBox.setUsername("");
        giftBox.session = createSession(giftBox);
        giftBox.setSpam(10);
        sendAndHandleMessages(giftBox);
        addGiftToZone(giftBox,zone);
        moveGift(giftBox);
    }

    public void createNearbyGiftBoxes(User boss, Zone zone, short x, short y, int baseGiftId) throws IOException {
        // Tạo hộp quà ở các vị trí gần Boss
        createGiftBox(zone, (short) (boss.getX()+(short)20),(short) (boss.getY()+(short)20),baseGiftId);
        createGiftBox(zone, (short) (boss.getX()-(short)20), (short) (boss.getY()-(short)20), baseGiftId + 1);
        createGiftBox(zone, (short) (boss.getX()+(short)20), (short) (boss.getY()-(short)20), baseGiftId + 2);
        createGiftBox(zone, (short) (boss.getX()-(short)20), (short) (boss.getY()+(short)20), baseGiftId + 3);
    }

    private void assignGiftItemToBoss(User boss) {
        // Gán item cụ thể cho hộp quà phân thân, nếu khác với Boss chính
        List<Integer> giftItems = Arrays.asList(2215, 2215, 2215); // Ví dụ các item cho hộp quà
        int randomItemId = giftItems.get(new Random().nextInt(giftItems.size()));
        boss.addItemToWearing(new Item(randomItemId));
    }

    //đồ của boss
    private void assignRandomItemToBoss(User boss) {
        List<Integer> itemIds1 = Arrays.asList(0,8,4121, 4122, 4123,4128,4563);//xen bo hung
        List<Integer> itemIds2 = Arrays.asList(0,8,6161, 6162, 6163,4128,4563);//ma bu
        List<Integer> itemIds3 = Arrays.asList(0,8,6164, 6165, 6166,4561);// yasua
        List<Integer> itemIds4 = Arrays.asList(0,8,4442, 4443, 4444,4562);// goku rose
        List<Integer> itemIds5 = Arrays.asList(0,8,5409,5410,5411, 5412, 5413);// lufi
        List<Integer> itemIds6 = Arrays.asList(0,8,7029,7030,7031, 7032, 7039);// ngáo bính


        Map<List<Integer>, String> itemListToName = new HashMap<>();
        itemListToName.put(itemIds1, "Xên bọ hung");
        itemListToName.put(itemIds2, "MaBư");
        itemListToName.put(itemIds3, "Yasua");
        itemListToName.put(itemIds4, "Goku rose");
        itemListToName.put(itemIds5, "Lufi");
        itemListToName.put(itemIds6, "Ngáo Bính");
//        itemListToName.put(itemIds7, "XuongKho");
//        itemListToName.put(itemIds7, "XacUop");

        List<List<Integer>> allItemLists = Arrays.asList(itemIds1,itemIds2,itemIds3,itemIds4,itemIds5,itemIds6);
//        List<List<Integer>> allItemLists = Arrays.asList(itemIds);
        Random random = new Random();
        int randomIndex = random.nextInt(allItemLists.size());
        List<Integer> randomList = allItemLists.get(randomIndex);
        String bossName = itemListToName.get(randomList);

        for (int itemId : randomList) {
            Item item = new Item(itemId);
            boss.addItemToWearing(item);
        }
        String bossUsername = generateRandomUsername(4).toLowerCase();
        String bossUsername1 = generateRandomUsername(3).toLowerCase();;
        boss.setUsername(bossUsername+bossName+bossUsername1);
    }

    private void sendAndHandleMessages(User boss) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeByte(0);
            dos.writeInt(1024);
            dos.writeUTF("MicroEmulator");
            dos.writeInt(512);
            dos.writeInt(1080);
            dos.writeInt(1920);
            dos.writeBoolean(true);
            dos.writeByte(0);
            dos.writeUTF("v1.0");
            dos.writeUTF("1");
            dos.writeUTF("2");
            dos.writeUTF("3");
            dos.flush();
            byte[] data = baos.toByteArray();

            MessageHandler handler = new MessageHandler(boss.session);
            handler.onMessage(new Message(Cmd.SET_PROVIDER, data));

            byte[] data2 = new byte[]{9};
            boss.session.getHandler(new Message(Cmd.GET_HANDLER, data2));
            if(boss.getId()>2000010000)
            {
                return;
            }
            MoveArea(boss);
        }
    }

    private void moveBoss(User boss) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(boss.getX());//x
            dos1.writeShort(boss.getY());//y
            int ranArea = Utils.nextInt(9);
            dos1.writeByte((byte)ranArea);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));;
        }
    }

    private void addGiftToZone(User gift,Zone zone) {
        ByteArrayOutputStream joinPank = new ByteArrayOutputStream();
        try (DataOutputStream dos2 = new DataOutputStream(joinPank)) {
            dos2.writeByte(zone.getMap().getId());
            dos2.writeByte(zone.getId());
            dos2.writeShort(gift.getX());//x
            dos2.writeShort(gift.getY());//y
            dos2.flush();
            byte[] dataJoinPak = joinPank.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(gift.session);
            parkMsgHandler1.onMessage(new Message(Cmd.AVATAR_JOIN_PARK, dataJoinPak));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveGift(User boss) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(boss.getX());//x
            dos1.writeShort(boss.getY());//y
            dos1.writeByte(0);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));
            //getMapService().chat(this, "ta đến rồi đây");
            //System.out.println("gift move : X = " + boss.getX() + ", y = " + boss.getY());

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    boss.session.close();
                }
            }, 120000); // 2 phút = 120000 ms

        }
    }

    private void moveBossXY(User boss,int x,int y) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(x);//x
            dos1.writeShort(y);//y
            dos1.writeByte(2);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));
            //System.out.println("boss move : X = " + boss.getX() + ", y = " + boss.getY());
        }
    }

    public static Session createSession(User boss){
        //Cmd.SET_PROVIDER
        try {
            // Tạo một Socket (thay thế bằng thông tin kết nối thực tế)
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 19128)); // Thay thế bằng địa chỉ IP và cổng thực tế
            int sessionId = boss.getId(); // Ví dụ về ID, có thể là bất kỳ giá trị nào phù hợp
            Session session = new Session(socket, sessionId);
            session.ip = "127.0.0.1";
            session.user = boss;
            session.connected = true;
            session.login = true;
            System.out.println("Session created with ID: " + session.id);
            return session;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Builder
    public void addChat(String chat) {
        textChats.add(chat);
    }
    @Override
    public void sendMessage(Message ms) {

    }
    public void BossSkillRanDomUser(byte skill1,byte skill2){
        Random rand = new Random();
        List<User> players = this.session.user.getZone().getPlayers();
        User randomPlayer = null;
        while (randomPlayer == null) {
            int rplayerIndex = rand.nextInt(players.size());
            User playerss = players.get(rplayerIndex);

            if (playerss.getId() < Npc.ID_ADD) {
                randomPlayer = playerss;
            }
        }
        for (User player : players) {
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill1)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 3)
                    .loopType((byte) 1)
                    .radius((short) 1)
                    .idPlayer(this.session.user.getId())
                    .send();
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill2)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 3)
                    .loopType((byte) 1)
                    .radius((short) 1)
                    .idPlayer(randomPlayer.getId())
                    .send();
        };
    }
    public static String generateRandomUsername(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    //    boss thông thường0
    public static void spawnBossesForMap(int mapId, int numBosses) {
        Utils random = null;
        avatar.play.Map m = MapManager.getInstance().find(mapId);
        List<Zone> zones = m.getZones();
        for (int i = 0; i < numBosses; i++) {
            Boss boss = new Boss(); // Tạo boss mới
            //List<String> chatMessages = Arrays.asList("YAAAA", "YOOOO");
            //((Boss) boss).setTextChats(chatMessages);
            boss.session = createSession(boss);

//            Zone randomZone = zones.get(random.nextInt(zones.size()));
            Zone randomZone = zones.get(0);
            try {
                boss.addBossToZone(boss,mapId,randomZone, (short) 50, (short) 50, (int) 50000);
                System.out.println("Boss " + i + " khu " + randomZone.getId() + " map " + mapId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // boss phát quà + di chuyển
    public static void spawnBossesForMapPhatQua(int mapId, int numBosses) {
        Utils random = null;
        avatar.play.Map m = MapManager.getInstance().find(mapId);
        List<Zone> zones = m.getZones();
        for (int i = 0; i < numBosses; i++) {
            Boss boss = new Boss(); // Tạo boss mới
            //List<String> chatMessages = Arrays.asList("YAAAA", "YOOOO");
//            ((Boss) boss).setTextChats(chatMessages);
            boss.session = createSession(boss);

//            Zone randomZone = zones.get(random.nextInt(zones.size()));
            Zone randomZone = zones.get(0);
            try {
                boss.addBossToZonePhatQua(boss,mapId,randomZone, (short) 50, (short) 50, (int) 5000000);
                System.out.println("Boss " + i + " khu " + randomZone.getId() + " map " + mapId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    1 boss chỉ định đặc biệt

    public void addBossToZonePhatQua(User boss,int Map ,Zone zone, short x, short y,int hp) throws IOException {
        if (bossCount >= TOTAL_BOSSES) {
            return; // Dừng nếu đã tạo đủ số lượng Boss
        }
        scheduler.schedule(() -> {
            try {
                LocalTime now = LocalTime.now();
                LocalTime tenAM = LocalTime.of(11, 0);
                LocalTime twoPM = LocalTime.of(14, 0);
                LocalTime sevenPM = LocalTime.of(19, 0);
                LocalTime elevenPM = LocalTime.of(23, 0);

                //tạo qu trong time
                if ((now.isAfter(tenAM) && now.isBefore(twoPM)) || (now.isAfter(sevenPM) && now.isBefore(elevenPM))) {
                    boss.getWearing().clear();
                    boss.setId(currentBossId++);
                    boss.setDefeated(true);

                    List<Integer> itmnoel = new ArrayList<>(Arrays.asList(0, 4, 2426, 2427, 2428, 2309, 14, 540));
                    List<Item> wearings = new ArrayList<>();
                    for (int i = 0; i < itmnoel.size(); i++) {

                        Item item = Item.builder().id(itmnoel.get(i))
                                .quantity(1)
                                .expired(-1)
                                .build();

                        if (item.reliability() > 0) {
                            wearings.add(item);
                        }

                        // Thêm items vào wearing của boss
                        for (Item item1 : wearings) {
                            boss.addItemToWearing(item1);
                        }
                        int randomIndex = RANDOM.nextInt(2);
                        if (randomIndex == 0) {
                            Item itemxe = Item.builder().id(4815)
                                    .quantity(1)
                                    .expired(-1)
                                    .build();
                            boss.addItemToWearing(itemxe);
                        } else {
                            Item itemxe = Item.builder().id(4002)
                                    .quantity(1)
                                    .expired(-1)
                                    .build();
                            boss.addItemToWearing(itemxe);
                        }

                        boss.setUsername("onggianoel");

                    }
                    boss.setPhatqua(true);
                    boss.setSoqua(5);
                    List<String> chatMessages = Arrays.asList("Bất ngờ chưa!!! ông rà lỏen đây.");
                    ((Boss) boss).setTextChats(chatMessages);
                    boss.bossMapId = Map;
                    bossCount++; // Tăng số lượng Boss đã tạo
                    sendAndHandleMessages(boss);
                    moveBoss(boss);
                    int mapId = zone.getMap().getId();
///
                    List<int[]> coordinates = zoneCoordinates.get(mapId);
                    if (coordinates != null && !coordinates.isEmpty()) {
                        int[] randomCoordinate = coordinates.get(new Random().nextInt(coordinates.size()));
                        moveBossXY(boss, randomCoordinate[0], randomCoordinate[1]);
                    } else {
                        // Nếu không có tọa độ nào, có thể xử lý trường hợp này ở đây
                        System.err.println("Không có tọa độ cho bản đồ ID " + mapId);
                    }
                }
                //boss.session.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 5, TimeUnit.SECONDS); // 4 giây trễ trước khi thực hiện các hành động khác
    }

    public synchronized void handleBossDefeatPhatQua(Boss boss) throws IOException {
        List<String> newMessages = Arrays.asList("tạm biệt mấy nhóc","Tạm biệt mấy nhóc ta đi phát chỗ khác đây!!!");
        this.textChats = new ArrayList<>(newMessages);

        for (String chatMessage : textChats) {
            getMapService().chat(boss, chatMessage);
            textChats.remove(chatMessage);
        }
        Utils random = null;
        avatar.play.Map m = MapManager.getInstance().find(boss.getBossMapId());
        List<Zone> zones = m.getZones();
        if(boss.getSoqua()<=0){
            Zone randomZone = zones.get(random.nextInt(zones.size()));
            boss.getZone().leave(boss);
            addBossToZonePhatQua(boss,boss.bossMapId,randomZone,(short) 0,(short) 0,1);
        }
    }
    public static void spawnBossAt(int mapId, int zoneId, short x, short y, int hp) {
        try {
            Boss boss = new Boss();
            boss.session = createSession(boss);
            avatar.play.Map map = MapManager.getInstance().find(mapId);
            Zone zone = map.getZoneById(zoneId);

            if (zone == null) {
                System.err.println("Không tìm thấy khu " + zoneId + " trong bản đồ " + mapId);
                return;
            }

            boss.addBossToZone(boss, mapId, zone, x, y, hp);
            System.out.println("Boss đã được tạo tại map " + mapId + " khu " + zoneId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Tìm map thiếu boss một cách hiệu quả

    public int findMapNeedingBoss() {
        Map<Integer, Integer> currentBosses = countAllBosses();

        return BOSS_REQUIREMENTS.entrySet().stream()
                .filter(entry -> currentBosses.get(entry.getKey()) < entry.getValue())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }
    // Phương thức đếm tất cả boss trong một lần duyệt
    public Map<Integer, Integer> countAllBosses() {
        Map<Integer, Integer> bossCount = new HashMap<>();

        // Khởi tạo với 0 cho tất cả map
        BOSS_REQUIREMENTS.keySet().forEach(mapId -> bossCount.put(mapId, 0));

        // Duyệt một lần qua tất cả các map
        for (Integer mapId : BOSS_REQUIREMENTS.keySet()) {
            avatar.play.Map map = MapManager.getInstance().find(mapId);
            if (map != null) {
                int count = map.getZones().stream()
                        .flatMap(zone -> zone.getPlayers().stream())
                        .filter(user -> user instanceof Boss)
                        .mapToInt(boss -> 1)
                        .sum();
                bossCount.put(mapId, count);
            }
        }
        return bossCount;
    }

}