
package avatar.handler;

import avatar.Farm.Animal;
import avatar.Farm.FarmAnimal;
import avatar.Farm.NongSan;
import avatar.common.BossShopItem;
import avatar.constants.Cmd;
import avatar.constants.NpcName;
import avatar.item.Item;
import avatar.item.Part;
import avatar.item.PartManager;
import avatar.minigame.TaiXiu;
import avatar.model.*;

import java.util.*;
import java.math.BigInteger;

import avatar.lucky.DialLucky;
import avatar.lucky.DialLuckyManager;

import java.util.concurrent.CountDownLatch;
import java.io.IOException;
import java.util.concurrent.*;
import avatar.network.Message;
import avatar.play.MapManager;
import avatar.play.NpcManager;
import avatar.play.Zone;
import avatar.server.*;
import avatar.service.AvatarService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static avatar.constants.NpcName.*;
import avatar.db.DbManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NpcHandler {

    private static final Map<Integer, Long> lastActionTimes = new HashMap<>();
    private static final long ACTION_COOLDOWN_MS = 90; // 2 giây cooldown


    public static void handleDiaLucky(User us, byte type) {
        DialLucky dl = DialLuckyManager.getInstance().find(type);
        if (dl != null) {
            if (dl.getType() == DialLuckyManager.MIEN_PHI) {
                if(us.chests.size() >= us.getChestSlot()-2){
                    us.getAvatarService().serverDialog("Bạn phải có ít nhất 3 ô trống trong rương đồ");
                    return;
                }
                Item itm = us.findItemInChests(593);
                if (itm.getQuantity() <=0 || itm.getQuantity() > 1998)
                {
                    return;
                }
                if (itm == null || itm.getQuantity() <= 0) {
                    us.getAvatarService().serverDialog("Bạn không có Vé quay số miễn phí!");
                    return;
                }
            }
            if (dl.getType() == DialLuckyManager.XU) {
                if(us.chests.size() >= us.getChestSlot()-2){
                    us.getAvatarService().serverDialog("Bạn phải có ít nhất 3 ô trống trong rương đồ");
                    return;
                }
                if (us.getXu() < 25000) {
                    us.getAvatarService().serverDialog("Bạn không đủ xu!");
                    return;
                }
            }
            if (dl.getType() == DialLuckyManager.LUONG) {
                if(us.chests.size() >= us.getChestSlot()-2){
                    us.getAvatarService().serverDialog("Bạn phải có ít nhất 3 ô trống trong rương đồ");
                    return;
                }
                if (us.getLuong() < 5) {
                    us.getAvatarService().serverDialog("Bạn không đủ lượng!!");
                    return;
                }
            }
        }
        us.setDialLucky(dl);
        dl.show(us);
    }

    public static void handlerCommunicate(int npcId, User us) throws IOException {
        Zone z = us.getZone();
        if (z != null) {
            User u = z.find(npcId);
            if (u == null) {
                return;
            }
        } else {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastActionTime = lastActionTimes.getOrDefault(us.getId(), 0L);

        if (currentTime - lastActionTime < ACTION_COOLDOWN_MS) {
            us.getAvatarService().serverDialog("Từ từ thôi bạn!");
            return;
        }
        // Cập nhật thời gian thực hiện hành động
        lastActionTimes.put(us.getId(), currentTime);
        int npcIdCase = npcId - Npc.ID_ADD;
        User boss = z.find(npcId);
        double maxDistance = 55.0;
        int playerX = us.getX();
        int playerY = us.getY();
        int bossX = boss.getX();
        int bossY = boss.getY();
        double distance = Utils.distanceBetween(playerX, playerY, bossX, bossY);

        if (npcIdCase > 1000 && npcIdCase<=9999)
        {
            Random random = new Random(); // Khởi tạo Random
            if (us.getRandomTimeInMillis() == 0) {
                int randomMinutes = 10 + random.nextInt(11); // Tạo số phút ngẫu nhiên từ 10-20
                long randomTimeInMillis = randomMinutes * 60 * 1000; // Chuyển đổi phút sang mili giây
                // Gán thời gian ngẫu nhiên cho user
                us.setRandomTimeInMillis(randomTimeInMillis);
                us.setLastTimeSet(System.currentTimeMillis()); // Lưu thời gian hiện tại khi gán
            }

            // Kiểm tra thời gian hiện tại và thời gian đã gán
            long currentTime1 = System.currentTimeMillis();
            System.out.println(currentTime1);
            if (currentTime1 - us.getLastTimeSet() >= us.getRandomTimeInMillis() ||us.getspamclickBoss()) {
                // Nếu thời gian đã hết, hiện thông báo rô bốt
                us.getAvatarService().openMenuOption(1000, 1,
                        "bạn có phải robot không? : Đúng rồi",
                        "robot là bạn hả ? : Chắc chắn rồi",
                        "robot hả : Không phải",
                        "bạn là robot : Yes sir");
                us.setspamclickBoss(true);
                us.setRandomTimeInMillis(0); // Đặt lại để lần sau có thể gán thời gian mới
                return;
            }
            if(us.getSession().isResourceHD()){
                List<Menu> listmenuboss = new ArrayList<>();
                Menu bossmenu = Menu.builder().name(" Đánh ").action(() -> {
                    us.getAvatarService().serverDialog("OK !");
                }).build();
                listmenuboss.add(bossmenu);
                us.setMenus(listmenuboss);
                us.getAvatarService().openMenuOption(npcId, 0, listmenuboss);
            }
            if (boss.getUsername() == "onggianoel") {
                us.getAvatarService().serverDialog("bình tĩnh ông cháu ơi");
                return;
            }
            if (boss.isDefeated()) {
                us.getAvatarService().serverDialog("boss đã chết");
                return;
            }
            if (distance > maxDistance) {
                us.getAvatarService().serverDialog("Bạn đứng xa rồi : v");
                return;
            }
            LocalTime now = LocalTime.now();

            // Đặt khoảng thời gian hợp lệ
            LocalTime startTime = LocalTime.of(7, 0); // 6h sáng
            LocalTime endTime = LocalTime.of(22, 59);  // 11h đêm

            // Kiểm tra nếu thời gian hiện tại nằm trong khoảng
            if (now.isAfter(startTime) && now.isBefore(endTime)) {
                us.updateXuKillBoss(+1);
            } else {
                // Xử lý nếu thời gian không nằm trong khoảng
                System.out.println("Hàm không được kích hoạt ngoài khoảng thời gian từ 6h sáng đến 11h đêm.");
            }
            us.updateXu(+us.getDameToXu());
            us.updateLuong(+10);
            us.getAvatarService().updateMoney(0);

            List<User> lstUs = us.getZone().getPlayers();

            int skill = us.getUseSkill();
            switch (skill) {
                case 0:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)23,(byte)24);
                    break;
                case 1:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)25,(byte)26);
                    break;
                case 2:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)12,(byte)13);
                    break;
                case 3:
                    // Thực hiện hành động khi skill = 3
                    break;
                case 4:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)38,(byte)39);
                    break;
                case 5:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)42,(byte)43);
                    break;
                case 6:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)36,(byte)37);
                    break;
                case 7:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)44,(byte)48);
                    break;
                default:
                    us.skillUidToBoss(lstUs,us.getId(),npcId,(byte)23,(byte)24);
                    break;
            }
            boss.updateHP(-us.getDameToXu(),(Boss)boss, us);


//            }else if (us.findItemInWearing(4715)!=null) {
//                boss.updateHP(-us.getDameToXu(),(Boss)boss, us);


        }
        else if (npcIdCase >= 10000) {
            if (distance > maxDistance) {
                us.getAvatarService().serverDialog("Bạn đứng xa quá");
                return;
            }
            if(boss.isSpam()){
                us.getAvatarService().serverDialog("hộp này đã nhặt");
                return;
            }
            us.updateSpam(-1,(Boss)boss,us);
        } else {
            switch (npcIdCase) {
                case NpcName.Than_Tai_Xiu: {
                    List<Menu> menuList = List.of(
                            Menu.builder().name("Cược bằng Xu").menus(
                                    List.of(
                                            Menu.builder().name("Cược Tài (Xu)").action(() -> {
                                                us.getAvatarService().sendTextBoxPopup(us.getId(), 0, "Nhập số xu bạn muốn cược vào Tài:", 1);
                                            }).build(),
                                            Menu.builder().name("Cược Xỉu (Xu)").action(() -> {
                                                us.getAvatarService().sendTextBoxPopup(us.getId(), 1, "Nhập số xu bạn muốn cược vào Xỉu:", 1);
                                            }).build()
                                    )).build(),

                            Menu.builder().name("Cược bằng Lượng").menus(
                                    List.of(
                                            Menu.builder().name("Cược Tài (Lượng)").action(() -> {
                                                us.getAvatarService().sendTextBoxPopup(us.getId(), 2, "Nhập số lượng bạn muốn cược vào Tài:", 1);
                                            }).build(),
                                            Menu.builder().name("Cược Xỉu (Lượng)").action(() -> {
                                                us.getAvatarService().sendTextBoxPopup(us.getId(), 3, "Nhập số lượng bạn muốn cược vào Xỉu:", 1);
                                            }).build()
                                    )).build(),

                            Menu.builder().name("lịch sử 10 ván").action(() -> {
                                TaiXiu.getInstance().viewGameRoundHistory(us);
                            }).build(),
                            Menu.builder().name("top cao thủ thắng và thua nhiều nhất").action(() -> {
                                TaiXiu.getInstance().getTopWinerXu(us);
                                TaiXiu.getInstance().getTopLossXu(us);
                                TaiXiu.getInstance().getTopWinLuong(us);
                                TaiXiu.getInstance().getTopLossLuong(us);
                                us.getAvatarService().serverDialog("check tin nhắn đi");
                            }).build(),
                            Menu.builder().name("Thành tích").action(() -> {
                                TaiXiu.getInstance().viewBetHistory(us);
                            }).build(),
                            Menu.builder().name("Thoát").action(() -> {
                            }).build()
                    );
                    us.setMenus(menuList);
                    us.getAvatarService().openMenuOption(npcId, 0, menuList);
                    break;
                }
                case NpcName.SAITAMA: {
                    List<Menu> QuanLyItem = new ArrayList<>();
                    Menu QuanLyeye = Menu.builder().name("Quản Lý Mặt").action(() -> {
                        List<Item> _chests = us.chests.stream().filter(item -> {
                            return item.getPart().getZOrder() == 30;
                        }).collect(Collectors.toList());
                        us.getAvatarService().viewChest(_chests);

                    }).build();
                    QuanLyItem.add(QuanLyeye);
                    QuanLyItem.add(Menu.builder().name("Quản Lý Mắt").action(() -> {
                        List<Item> _chests = us.chests.stream().filter(item -> {
                            return item.getPart().getZOrder() == 40;
                        }).collect(Collectors.toList());
                        us.getAvatarService().viewChest(_chests);
                    }).build());

                    QuanLyItem.add(Menu.builder().name("Xem Ô Rương Còn Trống").action(() -> {
                        int totalSlots = us.getChestSlot(); // Tổng số ô rương của người dùng
                        int usedSlots = us.chests.size();   // Số ô hiện đang sử dụng trong rương
                        int emptySlots = totalSlots - usedSlots; // Tính số ô trống còn lại

                        // Kiểm tra nếu còn trống, không thì hiển thị rương đã đầy
                        if (emptySlots > 0) {
                            us.getAvatarService().serverDialog("Số ô trống còn lại trong rương: " + emptySlots + "/" + totalSlots + " Rương Lv " + us.session.user.getChestLevel());
                        } else {
                            us.getAvatarService().serverDialog("Rương đã đầy!");
                        }
                    }).build());


                    QuanLyItem.add(Menu.builder().name("Xóa Vật Phẩm Trùng (Mặt và Mắt)").action(() -> {
                        // Lọc ra các vật phẩm có ZOrder là 30 hoặc 40
                        List<Item> filteredItems = us.chests.stream()
                                .filter(item -> {
                                    int zOrder = item.getPart().getZOrder();
                                    return zOrder == 30 || zOrder == 40;
                                })
                                .collect(Collectors.toList());

                        // Tạo một Map để giữ lại mỗi loại item duy nhất dựa trên ID
                        Map<Integer, Item> uniqueItemsMap = filteredItems.stream()
                                .collect(Collectors.toMap(
                                        Item::getId,         // Sử dụng ID để xác định vật phẩm trùng
                                        item -> item,
                                        (existing, duplicate) -> existing // Giữ lại item đầu tiên nếu trùng
                                ));

                        // Xóa các vật phẩm có ZOrder 30 hoặc 40 trong `chests`
                        us.chests.removeIf(item -> {
                            int zOrder = item.getPart().getZOrder();
                            return zOrder == 30 || zOrder == 40;
                        });

                        // Thêm lại các vật phẩm duy nhất vào `chests`
                        us.chests.addAll(uniqueItemsMap.values());

                        //us.getAvatarService().viewChest(us.chests);  // Hiển thị lại danh sách không còn vật phẩm trùng lặp
                        us.getAvatarService().serverDialog("ok bạn ơi");
                    }).build());

                    QuanLyItem.add(Menu.builder().name("Thoát").build());
                    us.setMenus(QuanLyItem);
                    us.getAvatarService().openMenuOption(npcId, 0, QuanLyItem);
                    break;
                }
                case NpcName.CHU_DAU_TU: {
                    List<Menu> chudautu = new ArrayList<>();
                    Menu chuDautu = Menu.builder().name("Mua biệt thự").action(() -> {
                        try {
                            us.session.BuyHouse();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).build();
                    chudautu.add(chuDautu);
                    us.setMenus(chudautu);
                    us.getAvatarService().openMenuOption(npcId, 0, chudautu);
                    break;
                }
                
                case NpcName.THAN_LONG: {
                    List<Menu> menu = new ArrayList<>();
                        Menu ongbut = Menu.builder().name("Luyện rồng").action(() -> {
                        try {
                            us.session.dragonInfo();
                        } catch (IOException ex) {
                        }
                        }).build();
                    menu.add(ongbut);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.RACING: {
                    List<Menu> menu = new ArrayList<>();
                        Menu racing = Menu.builder().name("Đổi siêu xe").action(() -> {
                        ShopTradeHandler.displayUI(us,0, 1214,3743,3742,4275,4671,4871,4016,6248,6339,6652);
                    }).build();
                    menu.add(racing);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.PHI_HANH_GIA: {
                    List<Menu> menu = new ArrayList<>();
                        Menu phg = Menu.builder().name("Mua đá vũ trụ").action(() -> {
                        ShopNpcHandler.displayUI(us, PHI_HANH_GIA, 2137,2138,2139,3672,5389);
                    }).build();
                    menu.add(phg);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.NHAN_AI: {
                    List<Menu> menu = new ArrayList<>();
                    menu.add(Menu.builder().name("Mua trái tim").action(() -> {
                        ShopNpcHandler.displayUI(us, PHI_HANH_GIA, 2806,2807,2808,2809,2810,3129,3130,3131,3132,3133,5144,5145,5146,5147,5148);
                    }).build());
                    menu.add(Menu.builder().name("Nâng cấp trái tim").action(() -> {
                        
                    }).build());
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.THO_REN: {
                    List<Menu> menu = new ArrayList<>();
                        Menu phg = Menu.builder().name("Mua vật phẩm").action(() -> {
                        ShopNpcHandler.displayUI(us, PHI_HANH_GIA, 2188);
                    }).build();
                    menu.add(phg);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.PHU_THUY_BONG_TOI: {
                    List<Menu> menu = new ArrayList<>();
                        Menu phuthuy = Menu.builder().name("Đổi vật phẩm").action(() -> {
                        ShopTradeHandler.displayUI(us,0, 1214,4834,4381,4382,4383,4384,4380,4372,3440,3441,3442,3363,3359,3324,3320,3321,3322,3323,2984,2354,2355,2349,2350,2059,2058,2057,2056,2055,2049,2050,2051,2047,2048,2045,2044,2042,2041,2040,2039);
                    }).build();
                    menu.add(phuthuy);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.PHU_THUY_NGUYEN_TO: {
                    List<Menu> menu = new ArrayList<>();
                    menu.add(Menu.builder().name("Đổi cánh").action(() -> {
                        ShopTradeHandler.displayUI(us,1, PHU_THUY_NGUYEN_TO,6008,5010,5011,5012,5401,6491);
                    }).build());
                    menu.add(Menu.builder().name("Đổi hào quang").action(() -> {
                        ShopTradeHandler.displayUI(us,2, PHU_THUY_NGUYEN_TO,5217,5269,5270,5310,5311);
                    }).build());
                    menu.add(Menu.builder().name("Đá ngũ hành?").action(() -> {
                        us.getAvatarService().customTab("Đá ngũ hành","Khi tham gia game. Bạn sẽ có 1% cơ hội nhận được đá ngũ hành trong các hoạt động sau:\n"
                                + "- Thu hoạch nông sản: Đá mộc\n"
                                + "- Đào khoáng sản: Đá thổ\n"
                                + "- Câu cá: Đá thủy\n"
                                + "- Đá kim: đang cập nhật\n"
                                + "- Đá hỏa: đang cập nhật");
                    }).build());
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }

                case NpcName.DAU_GIA: {
                    us.getAvatarService().serverDialog("đấu giá đang bảo trì các bạn vui lòng quay lại sau");
                    break;
                }
                
                case NpcName.BANG_HOI: {
                    List<Menu> menu = new ArrayList<>();
                    menu.add(Menu.builder().name("Thông tin bang hội")
                        .action(() -> {
                            try {
                                us.session.clanInfor();
                            } catch (IOException ex) {
                            }
                        })
                    .build());
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }

                case NpcName.VE_SO: {
                    LocalTime now = LocalTime.now();               
                    List<Menu> veso = new ArrayList<>();
                    if (now.isAfter(LocalTime.of(17, 1)) && now.isBefore(LocalTime.of(23, 59))
                            || now.isAfter(LocalTime.of(0, 0)) && now.isBefore(LocalTime.of(15, 59))) {
                    Menu Veso = Menu.builder().name("Mua vé số").action(() -> {
                        List<Item> itemveso = Part.shopByPart(PartManager.getInstance().getShop4());
                        if (itemveso == null) {
                            System.out.println("Khong tim thay item ve so");
                            return; // Handle the null case
                        }
                        us.getAvatarService().openUIShop(5, "Vé số", itemveso);
                    }).build();
                    veso.add(Veso);
                    }
                    veso.add(Menu.builder().name("Dò vé số").action(() -> {
                        
                        if (now.isAfter(LocalTime.of(16, 0)) && now.isBefore(LocalTime.of(17, 0))) {
                            int randomve = 330 + (int)(Math.random() * (429 - 330 + 1));
                            try(Connection connection = DbManager.getInstance().getConnection(); 
                                PreparedStatement ps = connection.prepareStatement("SELECT * FROM `veso` WHERE `id` = 1;");
                                ResultSet res = ps.executeQuery();) {
                                int savedNumberId = 0;  // Biến lưu id số đã lưu trong database
                                int savedNumber = 0;
                                    while (res.next()) {
                                    //savedNumberId = res.getInt("id");
                                    savedNumber = res.getInt("number");
                                    }
                                    if(savedNumber != 0)
                                    {
                                        int Num = savedNumber - 330;
                                        //us.getAvatarService().serverInfo("Số trúng hôm nay là "+Num+". Hãy đến công viên gặp NPC Vé số để nhận giải nhé");
                                        Item vesotrung = us.findItemInChests(savedNumber);
                                        if(vesotrung == null) {
                                            us.getAvatarService().serverDialog("Chúc bạn may mắn lần sau, vé trúng hôm nay là "+Num);
                                        
                                        }
                                        else {
                                            us.getAvatarService().serverDialog("Chúc mừng bạn đã trúng vé "+Num+". Bạn nhận được 1.000.000 xu");
                                            us.removeItemFromChests(vesotrung);
                                            us.updateXu(1000000);
                                        }
                                    }
                                    else
                                    {
                                        DbManager.getInstance().executeUpdate("UPDATE `veso` SET `number` = ? WHERE id = 1 LIMIT 1;", randomve);
                                        us.getAvatarService().serverDialog("Số may mắn hôm nay là "+randomve);
                                    }   
                                }
                            catch (SQLException e) {
                                    e.printStackTrace();
                            }
                            
                        }
                        else {
                            DbManager.getInstance().executeUpdate("UPDATE `veso` SET `number` = 0 WHERE id = 1 LIMIT 1;");
                            us.getAvatarService().serverDialog("Đã hết thời gian đổi vé, đổi vé từ 16 đến 17 giờ");
                        }
                        
                    }).build());
                    veso.add(Menu.builder().name("Bỏ vé không trúng").action(() -> {
                        
                    }).build());
                    veso.add(Menu.builder().name("Hướng dẫn").action(() -> {
                        us.getService().serverDialog("Hướng dẫn : Đang cập nhật");
                    }).build());
                    veso.add(Menu.builder().name("Thoát").build());
                    us.setMenus(veso);
                    us.getAvatarService().openMenuOption(npcId, 0, veso);
                    break;
                }
                    case NpcName.AN_XIN:{
                        List<Menu> listet = new ArrayList<>();
                        List<Item> Items = Part.shopByPart(PartManager.getInstance().getParts());
                        Menu quaySo = Menu.builder().name("vật phẩm").menus(
                                        List.of(
                                                Menu.builder().name("demo item").action(() -> {
                                                    us.getAvatarService().openUIShop(-49,"em.thinh",Items);
                                                }).build()
                                        ))
                                .id(npcId)
                                .npcName("donate đi")
                                .npcChat("show Item")
                                .build();
                        listet.add(quaySo);
                        listet.add(Menu.builder().name("Hướng dẫn").action(() -> {
                            us.getAvatarService().customTab("Hướng dẫn", "hãy nạp lần đầu để mở khóa mua =)))");
                        }).build());
                        listet.add(Menu.builder().name("Thoát").build());
                        us.setMenus(listet);
                        us.getAvatarService().openUIMenu(npcId, 0, listet, "donate đi", "");
                        break;
                    }
                case NpcName.QUAY_SO: {
                    List<Menu> qs = new ArrayList<>();
                    Menu quaySo1 = Menu.builder().name("Quay số").menus(
                                    List.of(
                                            Menu.builder().name("5 lượng").action(() -> {
                                                System.out.println("Action for 5 lượng triggered");
                                                handleDiaLucky(us, DialLuckyManager.LUONG);
                                            }).build(),
                                            Menu.builder().name("25.000 xu").action(() -> {
                                                System.out.println("Action for 15.000 xu triggered");
                                                handleDiaLucky(us, DialLuckyManager.XU);
                                            }).build(),
                                            Menu.builder().name("Q.S miễn phí").action(() -> {
                                                System.out.println("Action for Q.S miễn phí triggered");
                                                handleDiaLucky(us, DialLuckyManager.MIEN_PHI);
                                            }).build(),
                                            Menu.builder().name("Thoát").action(() -> {
                                                System.out.println("Exit menu triggered");
                                            }).build()
                                    ))
                            .id(npcId)
                            .build();
                    qs.add(quaySo1);
                    qs.add(Menu.builder().name("Xem hướng dẫn").action(() -> {
                        System.out.println("Action for Xem hướng dẫn triggered");
                        us.getAvatarService().customTab("Hướng dẫn", "Để tham gia quay số bạn phải có ít nhất 5 lượng hoặc 25 ngàn xu trong tài khoản và 3 ô trống trong rương\n Bạn sẽ nhận được danh sách những món đồ đặc biệt mà bạn muốn quay. Những món đồ đặc biệt này bạn sẽ không thể tìm thấy trong bất cứ shop nào của thành phố.\n Sau khi chọn được món đồ muốn quay bạn sẽ bắt đầu chỉnh vòng quay để quay\n Khi quay bạn giữ phím 5 để chỉnh lực quay sau đó thả ra để bắt đầu quay\n Khi quay bạn sẽ có cơ hội trúng từ 1 đến 3 món quà\n Quà của bạn nhận được có thể là vật phẩm bất kì, xu, hoặc điểm kinh nghiệm\n Bạn có thể quay được những bộ đồ bán bằng lượng như đồ hiệp sĩ, pháp sư...\n Tuy nhiên vật phẩm bạn quay được sẽ có hạn sử dụng trong một số ngày nhất định.\n Nếu bạn quay được đúng món đồ mà bạn đã chọn thì bạn sẽ được sở hữu món đồ đó vĩnh viễn.\n Hãy thử vận may để sở hữa các món đồ cực khủng nào !!!");
                    }).build());
                    qs.add(Menu.builder().name("Thoát").build());
                    us.setMenus(qs);
                    us.getAvatarService().openMenuOption(npcId, 0, qs);
                    break;
                }
                case NpcName.THO_KIM_HOAN: {
                    List<Menu> nangcap = new ArrayList<>();
                    String npcChat = "Muốn nâng cấp đồ thì vào đây";
                    Menu upgrade = Menu.builder().name("Nâng cấp").id(npcId).menus(
                                    List.of(
                                            Menu.builder().name("Nâng cấp xu").id(npcId)
                                                    .menus(listItemUpgrade(npcId, us, BossShopHandler.SELECT_XU))
                                                    .build(),
                                            Menu.builder().name("Nâng cấp lượng").id(npcId)
                                                    .menus(listItemUpgrade(npcId, us, BossShopHandler.SELECT_LUONG))
                                                    .id(npcId)
                                                    .build(),
                                            Menu.builder().name("Thoát").id(npcId).build()
                                    )
                            )
                            .build();
                    nangcap.add(upgrade);
                    nangcap.add(Menu.builder().name("Xem hướng dẫn")
                            .action(() -> {
                                us.getAvatarService().customTab("Hướng dẫn", "Nâng thì nâng không nâng thì cút!");
                            })
                            .build());
                    nangcap.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(nangcap);
                    us.getAvatarService().openMenuOption(npcId, 0, nangcap);
                    break;
                }
                case NpcName.LAI_BUON: {
                    List<Menu> laibuon = new ArrayList<>();
                    Menu LAI_BUON = Menu.builder().name("Điểm Danh").action(() -> {
                        //Item item = new Item(593, -1, 1);
                        //us.addItemToChests(item);
                        //us.addExp(5);
                        us.getService().serverMessage("đang xây dựng");//Bạn nhận được 5 điểm exp + 1 thẻ quay số miễn phí");
                    }).build();
                    laibuon.add(LAI_BUON);
                    laibuon.add(Menu.builder().name("Xem hướng dẫn").action(() -> {
                        us.getAvatarService().customTab("Hướng dẫn", "Đăng nhập mỗi ngày để nhận quà.\nDùng điểm chuyên cần để nhận đucợ những món quà có giá trị trong tương lai");
                    }).build());
                    laibuon.add(Menu.builder().name("Thoát").build());
                    us.setMenus(laibuon);
                    us.getAvatarService().openMenuOption(npcId, 0, laibuon);
                    break;
                }
                case NpcName.THO_CAU: {
                    List<Menu> thocau = new ArrayList<>();
                    Menu thoCau = Menu.builder().name("Câu cá").action(() -> {
                        List<Item> Items1 = new ArrayList<>();
                        Item item = new Item(446, 30, 0);//câu vip
                        Items1.add(item);
                        Item item1 = new Item(460, 2, 0);//vé cau
                        Items1.add(item1);
                        Item item2 = new Item(448, 30, 1);//mồi
                        Items1.add(item2);
                        us.getAvatarService().openUIShop(npcId, "Trùm Câu Cá,", Items1);
                        us.getAvatarService().updateMoney(0);
                    }).build();
                    thocau.add(thoCau);
                    thocau.add(Menu.builder().name("Bán cá").action(() -> {
                        try {
                            sellFish(us);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).build());
                    thocau.add(Menu.builder().name("Xem hướng dẫn").action(() -> {
                        us.getAvatarService().customTab("Hướng dẫn", "Câu cá kiếm được nhiều xu");
                    }).build());
                    thocau.add(Menu.builder().name("Thoát").build());
                    us.setMenus(thocau);
                    us.getAvatarService().openMenuOption(npcId, 0, thocau);
                    break;
                }
                case NpcName.MEOW_MEOW: {
                    List<Menu> listet = new ArrayList<>();
                    List<Item> Items = new ArrayList<>();

                    Menu quaySo = Menu.builder().name("shop noname").action(() ->{
                        List<Item> itemshop0 = Part.shopByPart(PartManager.getInstance().getShopHawaii1());

                        if (itemshop0 == null) {
                            System.out.println("Items list is null");
                            return; // Handle the null case
                        }
                        us.getAvatarService().openUIShop(5, "shop 0", itemshop0);
                    }).build();
                    listet.add(quaySo);            
                    listet.add(Menu.builder().name("Hướng dẫn").action(() -> {
                        us.getAvatarService().customTab("Hướng dẫn", "chua co huong dan");
                    }).build());
                    listet.add(Menu.builder().name("Thoát").build());
                    us.setMenus(listet);
                    us.getAvatarService().openUIMenu(npcId, 0, listet, "text 1", "text2");
                    break;
                }
                
                case NpcName.TIEN_CA: {
                    List<Menu> menu = new ArrayList<>();
                        Menu tienca = Menu.builder().name("Đổi vật phẩm").action(() -> {
                        ShopTradeHandler.displayUI(us,0, TIEN_CA,3122,3505);
                    }).build();
                    menu.add(tienca);
                    menu.add(Menu.builder().name("Thoát").id(npcId).build());
                    us.setMenus(menu);
                    us.getAvatarService().openMenuOption(npcId, 0, menu);
                    break;
                }
                
                case NpcName.ZOMBIE: {
                    List<Item> items = Part.shopByPart(PartManager.getInstance().getShop1());
                if (items == null) {
                    System.out.println("Items list is null");
                    return; // Handle the null case
                }
                us.getAvatarService().openUIShop(5, "Zombie", items);
                }
                case NpcName.TOP: {
                    List<Menu> bxhfarm = new ArrayList<>();
                    bxhfarm.add(Menu.builder().name("Top cao thủ level")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopLv();
                                us.getAvatarService().BxhLv(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Top cao thủ lượt đánh boss")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopdameboss();
                                us.getAvatarService().BxhenterBoss(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Top Sử dụng xu")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopuseCoin();
                                us.getAvatarService().BxhuseCoin(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Top Sử dụng lượng")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopuseGold();
                                us.getAvatarService().BxhuseGold(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Top đại gia Xu")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopMoney();
                                us.getAvatarService().BxhMoney(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Top đại gia Lượng")
                            .action(() -> {
                                List<User> topPlayers = us.getService().getTopMoneygold();
                                us.getAvatarService().BxhMoneyGold(topPlayers);
                            })
                            .build());
                    bxhfarm.add(Menu.builder().name("Thoát").build());
                    us.setMenus(bxhfarm);
                    us.getAvatarService().openMenuOption(NpcName.TOP, 0, bxhfarm);
                    break;
                }
                
            }
        }
    }
    

    public static void sellFish(User us) throws IOException {
        int[] array = {2130,2131,2132,454,455,456,457};

        for (int idFish : array) {
            Item item = us.findItemInChests(idFish); // Tìm item trong rương theo idFish

            // Nếu không tìm thấy item, tiếp tục với ID tiếp theo
            while (item != null && item.getQuantity() > 0) {
                int sellPrice = item.getPart().getCoin(); // Giá bán 1 món đồ
                String message = String.format("Bạn vừa bán 1 %s với giá = %d xu.", item.getPart().getName(), sellPrice);

                us.removeItemFromChests(item);
                us.updateXu(sellPrice); // Cập nhật xu

                us.getAvatarService().updateMoney(0); // Cập nhật tiền tệ
                us.getAvatarService().SendTabmsg(message); // Gửi thông báo bán hàng

                // Cập nhật lại item để kiểm tra số lượng
                item = us.findItemInChests(idFish);
            }
        }
    }

    public static void GopDiemSK(User us){
        java.util.Map<Integer, Integer> itemsToProcess = new HashMap<>();
        itemsToProcess.put(2383, 1);
        itemsToProcess.put(2384, 2);
        itemsToProcess.put(2385, 3);
        int addscores = 0;
// Lặp qua từng cặp ID và số lượng

        StringBuilder detailedMessage = new StringBuilder("Bạn đã đổi thành công:");
        for (java.util.Map.Entry<Integer, Integer> entry : itemsToProcess.entrySet()) {
            int itemId = entry.getKey();
            int scores = entry.getValue();
            Item item = us.findItemInChests(itemId);
            if (item != null && item.getQuantity() > 0) {
                addscores += item.getQuantity()*scores;
                detailedMessage.append(String.format("\n%s :(Điểm %d) Số lượng %d  x  tong %d điểm", item.getPart().getName(), scores, item.getQuantity(), item.getQuantity()*scores));
                us.updateScores(+addscores);
                us.removeItem(itemId, item.getQuantity());
            }
        }
        if(addscores > 0){
            detailedMessage.append(String.format("\n Tổng tất cả %d",addscores) +" điểm");
            us.getAvatarService().serverDialog(detailedMessage.toString());
        }else {
            us.getAvatarService().serverDialog("Bạn không còn kẹo");
        }
    }

    public static List<Menu> listItemUpgradeChay(int npcId, User us, byte type) {
        return List.of(
                Menu.builder()
                        .name("Nâng cấp item Chay To Win")
                        .id(npcId)
                        .action(() -> {
                            BossShopHandler.displayUI(us, BossShopHandler.SELECT_DNS, 3774,3776,3775,4157,5012,5010,5011,5401,6491,3855);
                        })
                        .build(),
                Menu.builder()
                        .name("Nâng cấp Item Quay Số/Nâng Cấp")//Nâng bằng đá ngũ sắc;
                        .id(npcId)
                        .action(() -> {
                            BossShopHandler.displayUI(us, BossShopHandler.SELECT_DNS, 4907,4119,5516,5099,5337,5342,5470,5667,5846,6575);
                        })
                        .build(),
                Menu.builder()
                        .name("Đổi item từ mảnh ghép")//đổi mảnh ghép thành item
                        .id(npcId)
                        .action(() -> {
                            BossShopHandler.displayUI(us, BossShopHandler.SELECT_ManhGhep, 6837);
                        })
                        .build()
        );
    }

    public static List<Menu> listItemUpgradePay(int npcId, User us, byte type) {
        return List.of(
                Menu.builder()
                        .name("Pay, Nâng cấp item")
                        .id(npcId)
                        .action(() -> {
                            BossShopHandler.displayUI(us, BossShopHandler.SELECT_HoaNS, 6671,6672,5012,5010,5011,5401,6491);
                        })
                        .build(),
                Menu.builder()
                        .name("Pay, Nâng cấp Item Quay Số/Nâng Cấp")
                        .id(npcId)
                        .action(() -> {
                            BossShopHandler.displayUI(us, BossShopHandler.SELECT_HoaNS, 6671,6672);
                        })
                        .build()
        );
    }

    public static List<Menu> listItemUpgrade(int npcId, User us, byte type) {
        //String npcName = "Thợ KH";
        //String npcChat = "Muốn đồ đang mặc đẹp hơn không? Ta có thể giúp bạn đấy";
        return List.of(
                Menu.builder().name("Quà cầm tay").id(npcId)
                        .menus(List.of(
                                        Menu.builder().name("Bông hoa cổ tích").action(() -> {
                                            BossShopHandler.displayUI(us, type, 6212, 6213, 6214);
                                        }).build(),
                                        Menu.builder().name("Hoa hồng phong thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5321, 5322, 5323);
                                        }).build(),
                                        Menu.builder().name("Hoa hồng xanh pha lê thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5286, 5287, 5288);
                                        }).build(),
                                        Menu.builder().name("Mộc thảo hồ điệp").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4160, 4161, 4162, 4163, 5050);
                                        }).build(),
                                        Menu.builder().name("Cung thần tình yêu thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4893, 4894, 4895);
                                        }).build(),
                                        Menu.builder().name("Cung xanh thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4890, 4891, 4892);
                                        }).build(),
                                        Menu.builder().name("Gậy thả thính mê hoặc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3507, 4218);
                                        }).build(),
                                        Menu.builder().name("Chong chóng thiên thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2238, 2239, 2274, 2275, 2404);
                                        }).build(),
                                        Menu.builder().name("Cục vàng huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2217, 2218, 2219, 2220, 2221, 2222, 2223);
                                        }).build()

                                )
                        )
                        .build(),
                Menu.builder().name("Nón").menus(List.of(
                                Menu.builder().name("Nón phù thuỷ hoả ngục truyền thuyết").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2411, 2412, 2413, 2414, 5503, 5504);
                                }).build(),
                                Menu.builder().name("Vương miện hoàng thân").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5394);
                                }).build(),
                                Menu.builder().name("Vương miện hoàng thân").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5391);
                                }).build(),
                                Menu.builder().name("Tôi thấy hoa vàng trên cỏ xanh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3266, 3267, 3268, 3269, 3954);
                                }).build(),
                                Menu.builder().name("Vương miện phép màu").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3422, 3423, 3639, 3640);
                                }).build(),
                                Menu.builder().name("Mũ ảo thuật tinh anh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2899, 2900, 2901, 2902, 2903, 3037, 3038, 3039);
                                }).build(),
                                Menu.builder().name("Vương miện huyền vũ").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2997, 2998, 2999);
                                }).build()
                        ))
                        .build(),
                Menu.builder().name("Trang phục")
                        .menus(
                                List.of(
                                        Menu.builder().name("Danh gia vọng tộc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5392, 5393);
                                        }).build(),
                                        Menu.builder().name("Nữ hoàng sương mai").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5054, 5055);
                                        }).build(),
                                        Menu.builder().name("Bá tước bóng đêm").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2876, 2877);
                                        }).build(),
                                        Menu.builder().name("Napoleon").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2231, 2232);
                                        }).build(),
                                        Menu.builder().name("Elizabeth").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2229, 2230);
                                        }).build()
                                )
                        )
                        .build(),
                Menu.builder().name("Cánh")
                        .menus(
                                List.of(
                                        Menu.builder().name("Cánh Thần Mặt Trời").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5312);
                                        }).build(),
                                        Menu.builder().name("Cánh chiến thần hắc hoá").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5971, 5972, 5973, 5974);
                                        }).build(),
                                        Menu.builder().name("Cánh quạ đen hoả ngục").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4332, 5313);
                                        }).build(),
                                        Menu.builder().name("Cánh tiểu thần phong linh").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2419, 2482, 2483, 2505, 2506, 5252, 5253);
                                        }).build(),
                                        Menu.builder().name("Cửu vỹ hồ ly thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4333, 4910, 4911, 4912, 4913, 4914, 4915, 4916, 4334, 4889);
                                        }).build(),
                                        Menu.builder().name("Cánh vàng ròng đa sắc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3376, 3377, 3404, 4897);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên thần tiên bướm").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4056, 4796);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên hồ tình yêu vĩnh cửu").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4196, 4435);
                                        }).build(),
                                        Menu.builder().name("Cánh băng hoả thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3448, 4057, 4375);
                                        }).build(),
                                        Menu.builder().name("Cánh hoả thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4311, 4312, 4313);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên sứ tình yêu").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2148, 2149, 2150, 2151, 2152, 3637);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên sứ").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2142, 2143, 2144, 2145, 2146, 3635);
                                        }).build(),
                                        Menu.builder().name("Cánh địa ngục hắc ám").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3529, 3530, 3531, 3532);
                                        }).build(),
                                        Menu.builder().name("Cánh cổng địa ngục").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3522, 3523, 3524, 3525, 3526, 3527);
                                        }).build(),
                                        Menu.builder().name("Cánh bướm đêm huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3366, 3379);
                                        }).build(),
                                        Menu.builder().name("Cánh băng giá huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3365, 3378);
                                        }).build(),
                                        Menu.builder().name("Cánh phép màu ước mơ").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2793, 2794, 2795, 2796);
                                        }).build(),
                                        Menu.builder().name("Cánh blue vững vàng").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2788, 2789, 2790, 2791);
                                        }).build()
                                )
                        )
                        .build(),
                Menu.builder().name("Thú cưng")
                        .menus(List.of(
//                                Menu.builder().name("Lang thần lãnh nguyên").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 5517, 5518);
//                                }).build(),
//                                Menu.builder().name("Thiên thần hồ điệp").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 5486, 5487);
//                                }).build(),
                                Menu.builder().name("Thiên thần hộ mệnh toàn năng").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5224, 5225, 5226);
                                }).build(),
                                Menu.builder().name("Cáo tuyết cửu vỹ").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4904, 4905);
                                }).build(),
                                Menu.builder().name("Cửu vỹ hồ ly").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4724, 4728, 4729);
                                }).build(),
//                                Menu.builder().name("Tiểu tiên bướm").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 4305, 5058);
//                                }).build(),
//                                Menu.builder().name("Ma vương").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 4096, 4731);
//                                }).build(),
//                                Menu.builder().name("Lợn lém lỉnh").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 4376);
//                                }).build(),
//                                Menu.builder().name("Tuần lộc tinh anh").action(() -> {
//                                    BossShopHandler.displayUI(us, type, 4323, 4324);
//                                }).build(),
                                Menu.builder().name("Bay max 2.0").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4079, 4080);
                                }).build(),
                                Menu.builder().name("Phương hoàng lửa").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3668, 3771, 3772, 3773, 3854);
                                }).build(),
                                Menu.builder().name("King Kong").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3744);
                                }).build(),
                                Menu.builder().name("Kỳ lân truyền thuyết").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2726, 2727, 2728, 2729, 2730);
                                }).build()
                        ))
                        .build(),
                Menu.builder().name("Tóc")
                        .menus(List.of(
                                Menu.builder().name("Tóc Siêu Xaya").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2019);
                                }).build()
                        ))
                        .build()
        );
    }

    public static void handlerAction(User us, int npcId, byte menuId, byte select) throws IOException {
        Zone z = us.getZone();
        if (z != null) {
            User u = z.find(npcId);
            if (u == null) {
                return;
            }
        } else {
            return;
        }
//        if (menuId == 0 && select == 0) {
//            // Trường hợp đặc biệt khi lần đầu mở menu
//            System.out.println("Initial menu open, displaying options without performing action.");
//            us.getAvatarService().openMenuOption(npcId, menuId,us.getMenus());
//            return;
//        }
//        int npcIdCase = npcId - 2000000000;
        List<Menu> menus = us.getMenus();
        if (menus != null && select < menus.size()) {
            Menu menu = menus.get(select);
            if (menu.isMenu()) {
                us.setMenus(menu.getMenus());
                us.getAvatarService().openUIMenu(npcId, menuId + 1, menu.getMenus(), menu.getNpcName(), menu.getNpcChat());
            } else if (menu.getAction() != null) {
                menu.perform();
            } else {
                switch (menu.getId()) {

                }
            }
            return;
        }
    }
}
