package avatar.service;

import avatar.Farm.*;
import static avatar.Farm.LandUpgradeCost.landUpgradeLevel1;
import static avatar.Farm.LandUpgradeCost.landUpgradeLevel2;
import avatar.db.DbManager;
import avatar.item.PartManager;
import avatar.lib.KeyValue;
import avatar.model.GameData;
import avatar.model.ImageInfo;
import avatar.constants.Cmd;
import avatar.item.Item;
import avatar.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Vector;
import avatar.server.Avatar;
import java.io.IOException;
import java.io.DataOutputStream;
import avatar.network.Message;
import avatar.network.Session;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.*;
import java.time.Duration;
import java.time.Month;
import java.util.Iterator;
import java.util.Random;

public class FarmService extends Service {

    private static final Logger logger = Logger.getLogger(Service.class);

    public FarmService(Session cl) {
        super(cl);
    }
    
public void Infor(User user) {
    user.getAvatarService().serverDialog(
        "Thông tin tài khoản: "+ this.session.user.getUsername() + "\n"
        + "Xu: " + this.session.user.getXu() + " - Lượng: " + this.session.user.getLuong() + " - Lượng khóa: " + this.session.user.getLuongKhoa() + "\n");
}
// Bán nông sản  
public void sellFarmitm(User user, Message ms) throws IOException {
    short idFarmItm = ms.reader().readShort();
    farmwarehouseitem fwi = PartManager.getInstance().findFarmwarehouseitemByID(idFarmItm);
    int price = 0; // Khai báo biến price bên ngoài vòng lặp
    boolean found = false;
    if(idFarmItm <= 100) {
        Iterator<NongSan> iterator = this.session.user.NongSan.iterator();
        while (iterator.hasNext()) {
            NongSan ns = iterator.next();
            if (idFarmItm == ns.getId()) {
                if(fwi.getId() == ns.getId()) {
                    price = ns.getSoluong() * fwi.getPrice(); // Tính giá bán dựa trên số lượng
                    //price = ns.getSoluong() * 3; // Tính giá bán dựa trên số lượng
                    iterator.remove(); // Xóa phần tử an toàn
                    System.out.println("Bán: " + ns.getId());
                    found = true; // Đánh dấu là đã tìm thấy
                    break; // Thoát khỏi vòng lặp khi tìm thấy
                }
                else {
                    
                }
            }
        }
    }
    else {
        Iterator<NongSanDacBiet> iteratordb = this.session.user.NongSanDacBiet.iterator();
        while (iteratordb.hasNext()) {
            NongSanDacBiet nsdb = iteratordb.next();
            if (idFarmItm == nsdb.getId()) {
                if(fwi.getId() == nsdb.getId()) {
                    price = nsdb.getSoluong() * fwi.getPrice();
                    //price = nsdb.getSoluong() * 10; // Tính giá bán dựa trên số lượng
                    iteratordb.remove(); // Xóa phần tử an toàn
                    System.out.println("Bán: " + nsdb.getId());
                    found = true; // Đánh dấu là đã tìm thấy
                    break; // Thoát khỏi vòng lặp khi tìm thấy
                }
                else {
                    
                }
            }
        }
    }
    if (!found) {
        user.getAvatarService().serverDialog("Không tìm thấy nông sản với ID: " + idFarmItm);
        return;
    }

    System.out.println("ID nông sản: " + idFarmItm + ", Giá bán: " + price);

    ms = new Message(Cmd.SELL_ITEM);
    DataOutputStream ds = ms.writer();
    ds.writeInt(price); // Gửi giá bán đến client
    ds.writeInt((int) this.session.user.getXu() + price); // Cập nhật tổng xu
    ds.writeShort(idFarmItm);
    ds.flush();
    this.session.sendMessage(ms); // Gửi message cho client
    this.session.user.updateXu(price); // Cập nhật xu người chơi
    user.getAvatarService().serverDialog(" Bạn đã bán " + fwi.getName() + " với giá " + price + " xu.");
}
 
    //trồng cây
// Phương thức trồng cây
public void plandSeed(Message ms) throws IOException, SQLException {
    int idUser = ms.reader().readInt();
    int indexCell = ms.reader().readByte();
    int idSeed = ms.reader().readByte();

    // Tìm hạt giống từ danh sách của người chơi
    HatGiong hd = this.session.user.findhatgiong(idSeed);
    FarmTree farmtree = PartManager.getInstance().findFarmtreeByID(idSeed);
    
    // Kiểm tra nếu ô đất tồn tại và người chơi có đủ hạt giống
    if (indexCell >= 0 && indexCell < this.session.user.landItems.size() && hd != null && hd.getSoluong() > 0) {
        // Lấy ô đất tại vị trí indexCell
        LandItem landItem = this.session.user.landItems.get(indexCell);
        
        // Kiểm tra xem ô đất có trống không (hoặc đã thu hoạch)
        if (landItem.getType() == -1 || landItem.getType() == 0) {  // Ô đất trống hoặc đã thu hoạch
            landItem.setType(idSeed); // Đặt cây mới vào ô đất
            hd.setSoluong(hd.getSoluong() - 1); // Giảm số lượng hạt giống

            // Reset các trạng thái của ô đất khi trồng cây mới
            //landItem.setType(seedId); // Đặt loại cây (id của hạt giống)
            landItem.setGrowthTime(farmtree.getHarvesttime()); // Thời gian tăng trưởng từ hạt giống
            landItem.setPlantedTime(LocalDateTime.now()); // Thời gian bắt đầu trồng cây
            //landItem.setPlantedTime(LocalDateTime.of(2020, 1, 1, 1, 1)); // Thời gian bắt đầu trồng cây test nhanh
            landItem.setHarvestable(false); // Không thể thu hoạch ngay
            landItem.setWatered(false); // Reset trạng thái tưới nước
            landItem.setFertilized(false); // Reset trạng thái bón phân
            landItem.setSucKhoe(100); // Sức khỏe cây bắt đầu ở mức tối đa
            landItem.setResourceCount(0); // Tài nguyên bắt đầu từ 0
        } else {
            // Nếu ô đất đã có cây, không thể trồng thêm
            System.out.println("Ô đất đã có cây, không thể trồng cây mới.");
            this.session.user.getAvatarService().serverDialog("Ô đất này đã có cây, vui lòng thu hoạch trước khi trồng cây mới.");
            return;
        }
    } else {
        // Nếu ô đất không hợp lệ hoặc người chơi không có đủ hạt giống
        System.out.println("Trong cay moi");
        //this.session.user.getAvatarService().serverDialog("Ô đất không hợp lệ hoặc bạn không có đủ hạt giống.");
        return;
    }

    // Gửi thông tin đã trồng cây thành công đến client
    ms = new Message(Cmd.PLANT_SEED); 
    DataOutputStream ds = ms.writer();
    ds.writeInt(idUser);
    ds.writeByte(indexCell); // Chỉ số ô đất
    ds.writeByte(idSeed); // Mã hạt giống đã trồng
    ds.flush();
    this.session.sendMessage(ms); // Gửi message cho client

    // Cập nhật thêm thông tin từ server
    System.out.println("Trồng cây thành công tại ô đất: " + indexCell);
}


    //thu hoạch
    public void treeHarvest(Message ms) throws IOException {
        int idfarm = ms.reader().readInt();//idUser
        byte indexcell = ms.reader().readByte();
        ms = new Message(Cmd.TREE_HARVEST);
        DataOutputStream ds = ms.writer();
        ds.writeByte(indexcell);
        if (indexcell >= 0 && indexcell < this.session.user.landItems.size()) {
            // Lấy ô đất tại vị trí indexCell và cập nhật thông tin
            LandItem landItem = this.session.user.landItems.get(indexcell);
            FarmTree farmtree = PartManager.getInstance().findFarmtreeByID(landItem.getType());
            landItem.setType(-1);
            int sanluong = (farmtree.getNumproduct()%100) * landItem.getSucKhoe()/100;
            ds.writeShort(sanluong);//so luong thu hoach duoc
            // đá mộc tỉ lệ 1%
            if (new Random().nextInt(100) < 1) {
                Item damoc = this.session.user.findItemInChests(5006);
                if(damoc != null) {
                    int quantity = damoc.getQuantity();
                    damoc.setQuantity(quantity+1);                
                }
                else {
                    this.session.user.addItemToChests(new Item(5006, -1, 1));
                }
                this.session.user.getAvatarService().chatTo("admin","Bạn nhận được 1 đá mộc",-1);
            }
            //
            int product = farmtree.getProductid();
            if(farmtree.getId() > 10) {
                boolean check = false;
                for(NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                    if(nsdb.getId() == farmtree.getProductid()) {
                        int sanluongtang = nsdb.getSoluong() + sanluong;
                        nsdb.setSoluong(sanluongtang);
                        check = true;
                    }
                }
                if(!check) {
                    this.session.user.NongSanDacBiet.add(new NongSanDacBiet(farmtree.getProductid(),sanluong));
                }
            }
            else {
                boolean check = false;
                for(NongSan ns : this.session.user.NongSan) {
                    if(farmtree.getProductid() == ns.getId()) {
                        int sanluongtang = ns.getSoluong() + sanluong;
                        ns.setSoluong(sanluongtang);
                        check = true;
                    }
                }
                if(!check) {
                     this.session.user.NongSan.add(new NongSan(farmtree.getProductid(), sanluong));
                }
            }
            System.out.println("THONG TIN");
            System.out.println("ID: " + farmtree.getId());
            System.out.println("Ten: " + farmtree.getName());
            System.out.println("San luong: " + sanluong);
        } else {
            // Nếu ô đất không tồn tại, có thể xử lý ngoại lệ hoặc thông báo lỗi
            System.out.println("Ô đất không tồn tại hoặc indexCell không hợp lệ.");
        }
        //ds.writeShort(sanluong);//so luong thu hoach duoc
        ds.flush();
        this.session.sendMessage(ms);
    }
public void harvestStarFruil(Message ms) throws IOException {
    ms = new Message(Cmd.HARVEST_STARFRUIT);
    DataOutputStream ds = ms.writer();
    ds.writeShort(215); // id cây khế

    for (Starfruil sf : this.session.user.starfruil) {
        // Kiểm tra xem cây có khế để thu hoạch không
        if (sf.getQuantity() > 0) {
            ds.writeShort(sf.getQuantity()); // ghi số lượng khế thu hoạch

            // Cập nhật kho
            boolean check = false;
            for (NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                if (nsdb.getId() == 215) {
                    int sanluongtang = nsdb.getSoluong() + sf.getQuantity();
                    nsdb.setSoluong(sanluongtang); // Cộng sản lượng vào kho
                    check = true;
                    break;
                }
            }
            if (!check) {
                this.session.user.NongSanDacBiet.add(new NongSanDacBiet(215, sf.getQuantity()));
            }
            sf.setQuantity(0);
            sf.setPlantedTime(LocalDateTime.now().plusSeconds(3600));
        } else {
            this.session.user.getAvatarService().serverDialog("Chưa đủ thời gian để thu hoạch lại");
        }
    }

    ds.flush();
    this.session.sendMessage(ms);
    this.session.user.getAvatarService().serverDialog("Thu hoạch khế thành công");
}

public void InforStarfruil(User user) {
    for (Starfruil sf : this.session.user.starfruil) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(sf.getPlantedTime()) || now.isEqual(sf.getPlantedTime())) {
            this.session.user.getAvatarService().serverDialog("Cây khế đã sẵn sàng để thu hoạch! Sản lượng: " + sf.getQuantity());
        } else {
            // Nếu chưa đến thời gian trồng, tính thời gian chờ còn lại
            long secondsRemaining = Duration.between(now, sf.getPlantedTime()).getSeconds();
            this.session.user.getAvatarService().serverDialog("Còn lại " + secondsRemaining + " giây nữa mới đến thời gian thu hoạch");
        }
    }
}

// nâng cấp cây khế
public void updateStarFruil(Message ms) throws IOException {
    // Tạo một tin nhắn để gửi đi
    ms = new Message(Cmd.UPDATE_STARFRUIL);
    DataOutputStream ds = ms.writer();

    // Số xu yêu cầu và thời gian nâng cấp (để bạn có thể thay đổi theo yêu cầu)
    int requiredMoney = 1000000; // Số xu yêu cầu
    int upgradeTime = 60;     // Thời gian nâng cấp 60 phútt

    // Kiểm tra xem người chơi có đủ xu không
    if (this.session.user.getXu() >= requiredMoney) {
        // Nếu đủ xu, thực hiện nâng cấp
        this.session.user.updateXu(-requiredMoney);  // Trừ xu của người chơi
        ds.writeByte(1);                               // Thành công
        ds.writeInt(requiredMoney);                    // Số xu đã chi
        for(Starfruil sf : this.session.user.starfruil) {
            sf.setTimeToUpgrade(LocalDateTime.now().plusMinutes((int)upgradeTime));
            sf.setTimeToHarvest(1);
        }
        ds.writeShort(upgradeTime);                    // Thời gian nâng cấp
        ds.flush();
        this.session.sendMessage(ms);

        // Thông báo nâng cấp thành công
        this.session.user.getAvatarService().serverDialog("Nâng cấp cây khế thành công!");
    } else {
        // Nếu không đủ xu, thông báo lỗi
        ds.writeByte(0);  // Thất bại
        ds.flush();
        this.session.sendMessage(ms);

        // Thông báo không đủ xu
        this.session.user.getAvatarService().serverDialog("Không đủ xu để nâng cấp cây khế.");
    }
}

   // check ô đất
   public void doRequestslot(Message ms,User us) throws IOException {
       int id = ms.reader().readInt();//id user
       ms = new Message(Cmd.REQUEST_SLOT);
       DataOutputStream ds = ms.writer();
       int currentLandSize = us.getLandItems().size(); // Số ô đất hiện tại của user
       LandUpgradeCost.Cost upgradeCost = landUpgradeLevel1.get(currentLandSize + 1); // Lấy giá nâng cấp cho ô đất tiếp theo
       if (upgradeCost != null) {
           int xuCost = upgradeCost.getXu();
           int luongCost = upgradeCost.getLuong();
           ds.writeUTF("Bạn có muốn mở ô đất " + (currentLandSize + 1) +
                   " cấp 1 với giá " + xuCost + " xu hoặc " + luongCost + " lượng không?");
       } else {
           us.getAvatarService().serverDialog("hiện tại chưa có giá ô đất");
           return;
       }
       ds.flush();
       this.session.sendMessage(ms);
   }

    public void doRequestslot2(Message ms,User us) throws IOException {
        int step = ms.reader().readByte();
        try {
            if (ms.reader().available() > 0) {
                int type = ms.reader().readByte();
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc giá trị 'type', sử dụng giá trị mặc định.");
        }
        ms = new Message(Cmd.REQUEST_SLOT);
        DataOutputStream ds = ms.writer();
        int currentLandSize = us.getLandItems().size();
        if(step>=0 && currentLandSize==48){
            int index = indexCellLevel2(this.session.user.getLandItems());
            LandUpgradeCost.Cost upgradeCost = landUpgradeLevel2.get(index); // Lấy giá nâng cấp cho ô đất tiếp theo
            if (upgradeCost != null) {
                int xuCost = upgradeCost.getXu();
                int luongCost = upgradeCost.getLuong();
                ds.writeUTF("Bạn có muốn mở ô đất " + (index+1) +
                        " (cấp 2) với giá " + xuCost + " xu hoặc " + luongCost + " lượng không?");
            } else {
                us.getAvatarService().serverDialog("hiện tại chưa có giá ô đất");
                return;
            }
            ds.flush();
            this.session.sendMessage(ms);
        }
    }
    
//mở ô đất
    private int indexCellLevel2(List<LandItem> landItems) {
        for(int i = 0;i < landItems.size(); i++){
            LandItem landItem = this.session.user.getLandItems().get(i);
            if (landItem.getlevel() == 1) {
                return i;
            }
        }
        return 99;
    }

    public void openLand(Message ms,User us) throws IOException {
        int id = ms.reader().readInt(); // ID của nông trại
        byte typeBuy = ms.reader().readByte(); // Loại giao dịch hoặc mã người dùng
        int currentLandSize = us.getLandItems().size(); // Số ô đất hiện tại của người chơi
        LandUpgradeCost.Cost upgradeCost;
        int xuCost;
        int luongCost;
        if(currentLandSize==48){
            for(int i = 0;i < currentLandSize; i++){
                LandItem landItem = us.getLandItems().get(i);
                if (landItem.getlevel() == 1) {
                    upgradeCost = landUpgradeLevel2.get(i); // Lấy giá nâng cấp cho ô đất tiếp theo
                    xuCost = upgradeCost.getXu();
                    luongCost = upgradeCost.getLuong();
                    if (typeBuy == 1) {
                        if (us.getXu() >= xuCost) {
                            SuccessOpenLand2(id,typeBuy,xuCost,"Xu",i);
                        } else {
                            this.session.user.getAvatarService().serverDialog("Bạn không đủ lượng để mở ô đất.");//ok
                        }
                    } else if (typeBuy == 2) { // Người chơi chọn mua bằng lượng
                        if (us.getLuong() >= luongCost) {
                            us.updateLuong(-luongCost); // Trừ lượng
                            SuccessOpenLand2(id,typeBuy,luongCost,"Lượng",i);                }
                        else {
                            this.session.user.getAvatarService().serverDialog("Bạn không đủ lượng để mở ô đất.");
                        }
                    } else {
                        this.session.user.getAvatarService().serverDialog("ERROR OPEN LAND.");
                    }
                    return;
                }
            }
        }
        upgradeCost = landUpgradeLevel1.get(currentLandSize + 1); // Lấy giá nâng cấp cho ô đất tiếp theo
        if (upgradeCost == null) {
            this.session.user.getAvatarService().serverDialog("Không có dữ liệu nâng cấp cho ô đất này");
            return;
        }
        xuCost = upgradeCost.getXu();
        luongCost = upgradeCost.getLuong();

        if (typeBuy == 1) {
            if (us.getXu() >= xuCost) {
                SuccessOpenLand(id,typeBuy,xuCost,"Xu");
            } else {
                this.session.user.getAvatarService().serverDialog("Bạn không đủ lượng để mở ô đất.");//ok
            }
        } else if (typeBuy == 2) { // Người chơi chọn mua bằng lượng
            if (us.getLuong() >= luongCost) {
                us.updateLuong(-luongCost); // Trừ lượng
                SuccessOpenLand(id,typeBuy,luongCost,"Lượng");                }
            else {
                this.session.user.getAvatarService().serverDialog("Bạn không đủ lượng để mở ô đất.");
            }
        } else {
            this.session.user.getAvatarService().serverDialog("ERROR OPEN LAND.");
        }
    }



    private void SuccessOpenLand(int id,byte typeBuy,int amout, String resourceType) throws IOException {
        Message ms = new Message(Cmd.OPEN_LAND);
        DataOutputStream ds = ms.writer();
        ds.writeInt(id);
        ds.writeInt(1);
        ds.writeByte(typeBuy);
        ds.writeUTF("Đã mở ô đất thành công, bạn đã bị trừ " + amout + " "+resourceType);
        ds.writeInt((int)this.session.user.getXu());
        ds.writeInt(this.session.user.getLuong());
        ds.writeInt(0);
        ds.flush();
        this.session.sendMessage(ms);
        this.session.user.landItems.add(new LandItem(1,0, -1,0, 0, true, false, false, LocalDateTime.now())); // Ô đất mặc định
    }

    private void SuccessOpenLand2(int id,byte typeBuy,int amout, String resourceType,int index) throws IOException {
        Message ms = new Message(Cmd.OPEN_LAND);
        DataOutputStream ds = ms.writer();
        ds.writeInt(id);
        ds.writeInt(2);
        ds.writeByte(typeBuy);
        ds.writeUTF("Đã mở ô đất thành công "+(index+1) + " đất level 2, bạn đã bị trừ " + amout + " "+resourceType);
        ds.writeInt((int)this.session.user.getXu());
        ds.writeInt(this.session.user.getLuong());
        ds.writeInt(0);
        ds.flush();
        this.session.sendMessage(ms);

        this.session.user.getLandItems().get(index).setlevel(2);
    }


    public void setBigFarm(Message ms) throws IOException {


        ms = new Message(51);
        DataOutputStream ds = ms.writer();
        int[] images = {99, 206};
        ds.writeByte(images.length);
        for (int i = 0; i < images.length; ++i) {
            ds.writeShort(i);
            ds.writeShort(images[i]);
        }
        ds.writeInt(15378);
        ds.writeInt(62724);
        ds.flush();
        this.session.sendMessage(ms);


        //apk goc
//        ms = new Message(51);
//        DataOutputStream ds = ms.writer();
//        short b19 = 2;
//        ds.writeByte(b19);
//        short[] array2 = {0, 1};
//        for (short value : array2) {
//            ds.writeShort(value);
//        }
//        short[] array3 = {66, 6};
//        for (short value : array3) {
//            ds.writeShort(value);
//        }
//        ds.writeInt(15378);
//        ds.writeInt(59669);
//        ds.flush();
//        this.session.sendMessage(ms);
    }

    public void Buy_item_farm(Message ms) throws IOException {
        short id = ms.reader().readShort();
        byte n = ms.reader().readByte();
        byte type = ms.reader().readByte();
        System.out.println("item farm id "+id+" sl "+n+" type sell"+type);

        FarmTree farmtree = PartManager.getInstance().findFarmtreeByID(id);

        if(type == 1){
            if(this.session.user.getXu()< farmtree.getPricexu()*n){
                this.session.user.getAvatarService().serverDialog("bạn không đủ xu");
                return;
            }
        } else{
            if(this.session.user.getLuong()< farmtree.getPriceluong()*n){
                this.session.user.getAvatarService().serverDialog("bạn không đủ lượng");
                return;
            }
        }
        HatGiong hdid = this.session.user.findhatgiong(id);
        if(hdid != null){
            if((hdid.getSoluong()+n) >99){
                this.session.user.getAvatarService().serverDialog("số lượng nhiều rồi");
                return;
            }
        }
        ms = new Message(62);
        DataOutputStream ds = ms.writer();
        ds.writeShort(id);
        ds.writeByte(n);
        if(type == 1) {
            ds.writeInt((int) this.session.user.getXu());//.newMoney
            this.session.user.updateXu(-farmtree.getPricexu()*n);
        }
        else {
            ds.writeInt((int) this.session.user.getLuong());//.newMoney
            this.session.user.updateXu(-farmtree.getPriceluong()*n);
        }
        ds.writeByte(type);
        ds.writeInt((int) this.session.user.getXu());
        ds.writeInt(this.session.user.getLuong());//luong
        ds.writeInt(0);//luongK
        ds.flush();
        this.session.sendMessage(ms);
        if(id <=42) {
            this.session.user.hatgiong.add(new HatGiong(id,n));
        }
        else {
            this.session.user.PhanBon.add(new PhanBon(id,n));
        }
    }

   public void Buy_ANIMAL(Message ms) throws IOException {
    byte n = ms.reader().readByte(); // ID động vật
    byte type = ms.reader().readByte(); // Loại tiền (xu hoặc vàng)

    FarmAnimal farmanimal = PartManager.getInstance().findFarmAnimalByID(n);
    if (farmanimal == null) {
        this.session.user.getAvatarService().serverDialog("Vật nuôi không tồn tại!");
        return;
    }

    // Kiểm tra tiền
    if (type == 1) { // Mua bằng xu
        if (this.session.user.getXu() < farmanimal.getPricexu()) {
            this.session.user.getAvatarService().serverDialog("Bạn không đủ xu!");
            return;
        }
        this.session.user.updateXu(-farmanimal.getPricexu()); // Trừ xu
    } else { // Mua bằng lượng
        if (this.session.user.getLuong() < farmanimal.getPriceluong()) {
            this.session.user.getAvatarService().serverDialog("Bạn không đủ lượng!");
            return;
        }
        this.session.user.updateLuong(-farmanimal.getPriceluong()); // Trừ lượng
    }
    int levelfish = this.session.user.lvfish + 1;
    int levelanimal = this.session.user.lvanimal + 1;
    // cá
    int ca = 0;
    int rua = 0;
    // nông trại
    int ga = 0;
    int bo = 0;
    int heo = 0;
    int cho = 0;
    int cuu = 0;
    int vit = 0;
    int trau = 0;
    for (Animal animal : this.session.user.Animal) {
        if (animal.getId() == 54) {
            ca++;
        }
        else if (animal.getId() == 59) {
            rua++;
        }
        else if (animal.getId() == 50) {
            ga++;
        }
        else if (animal.getId() == 51) {
            bo++;
        }
        else if (animal.getId() == 52) {
            heo++;
        }
        else if (animal.getId() == 53) {
            cho++;
        }
        else if (animal.getId() == 55) {
            cuu++;
        }
        else if (animal.getId() == 56) {
            vit++;
        }
        else if (animal.getId() == 58) {
            trau++;
        }
    

    // Nếu tổng số lượng động vật ID 54 và 59 lớn hơn level, thông báo hồ cá đã đầy
    if(n== 54 || n==59) 
        {
            if ((ca + rua) == levelfish) {
                this.session.user.getAvatarService().serverDialog("Hồ cá đã đầy");
                return;
            }
        }
    else
        {
            if ((ga + bo + heo + cho + cuu + vit + trau) == (levelanimal + 5)) {
                this.session.user.getAvatarService().serverDialog("Nông trại đã đầy");
            return;
            }
        }
    }
    // Gửi phản hồi về client
    ms = new Message(Cmd.BUY_ANIMAL);
    DataOutputStream ds = ms.writer();
    ds.writeByte(n); // ID vật nuôi
    if(type == 1) {
        ds.writeInt(farmanimal.getPricexu()); // Giá vật nuôi
    }
    else {
        ds.writeInt(farmanimal.getPriceluong()); // Giá vật nuôi
    }
    //ds.writeInt(price); // Không rõ mục đích
    ds.writeByte(type); // Loại tiền  
    ds.writeInt((int)this.session.user.getXu()); // Số xu sau khi mua
    ds.writeInt((int)this.session.user.getLuong()); // Số lượng sau khi mua
    ds.writeInt(0); // luongK
    ds.flush();
    this.session.sendMessage(ms);

   // Lấy thời gian hiện tại (timestamp) khi tạo động vật
long bornTime = System.currentTimeMillis();  // Lấy thời gian hiện tại

// Tạo động vật mới với bornTime là thời gian hiện tại
Animal newAnimal = new Animal(n, (long)bornTime, 0, 100, 1, 1, false, false, false);
    this.session.user.Animal.add(newAnimal);

    System.out.println("Mua vật nuôi: " + farmanimal.getName() + " Loại: " + type + " Giá: " + farmanimal.getPricexu() + "/"+farmanimal.getPriceluong()+"Thời gian:"+bornTime);
}
   // Giá động vật - Build
   public void priceAnimal(Message ms) throws IOException {
    byte i1 = ms.reader().readByte(); //0
    byte i2 = ms.reader().readByte(); //0
    byte i3 = ms.reader().readByte(); //0
    byte i4 = ms.reader().readByte(); //0
    byte i5 = ms.reader().readByte(); //index
    ms = new Message(Cmd.PRICE_ANIMAL);
    DataOutputStream ds = ms.writer();
    ds.writeByte(i5);
    ds.writeUTF("Giá con vật này là 950 xu. Bạn có muốn bán");
    ds.flush();
    this.session.sendMessage(ms);
    System.out.println("farm_msg72 : "+i1+" "+i2+" "+i3+" "+i4+" "+i5);
    }
  // Bán động vật - Build 
   public void sellAnimal(Message ms) throws IOException {
        int idFarm = ms.reader().readInt();
        byte i = ms.reader().readByte();
        ms = new Message(Cmd.SELL_ANIMAL);
        DataOutputStream ds = ms.writer();
        ds.writeInt(idFarm);
        ds.writeByte(i);
        ds.writeInt((int)this.session.user.getXu() + 950);
        ds.flush();
        this.session.sendMessage(ms);
        this.session.user.getAvatarService().updateMoney(0);
        System.out.println("farm_msg73 : "+idFarm+"/"+i);
        this.session.user.Animal.remove(i);
        
    }
   

    public void getBigFarm(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = this.session.getResourcesPath() + "bigFarm/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(54);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.writeShort(dat.length);
        for (int i = 0; i < dat.length; ++i) {
            ds.writeByte(dat[i]);
        }
        ds.flush();
        this.session.sendMessage(ms);
    }

    public void getImageData() {
        try {
            List<ImageInfo> imageInfos = GameData.getInstance().getFarmImageDatas();
            Message ms = new Message(Cmd.GET_IMAGE_FARM);
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
            logger.debug("getImageData: " + e.getMessage());
        }
    }

    public void getTreeInfo(Message ms) throws IOException {
        byte[] dat = Avatar.getFile("res/data/farm_info.dat");
        if (dat == null) {
            return;
        }
        System.out.println("TreeInfor: "+dat);
        ms = new Message(Cmd.GET_TREE_INFO);
        DataOutputStream ds = ms.writer();
        ds.write(dat);
        ds.flush();
        this.session.sendMessage(ms);
    }
    // nâng cấp đất
    public void updateLand(Message ms) throws IOException {
        return;
    }
    // nấu ăn
     public void Cooking(Message ms) throws IOException {
        short id = ms.reader().readShort();
        System.out.println("Cooking ID: " + id);
        if(id == -1) {
            ms = new Message(Cmd.COOKING);
            DataOutputStream ds = ms.writer();
            ds.writeShort(-1);
            ds.flush();
            this.session.sendMessage(ms);
            this.session.user.Cooking.remove(0);
            this.session.user.getAvatarService().serverDialog("Hủy thành công món ăn");
        }
        else if(id == 8 || id == 11 || id == 12 || id == 13 || id == 14 || id == 16 || id == 17 || id == 21 || id == 22 || id == 23 || id == 24 || id == 26) {
            FarmCooking farmcooking = PartManager.getInstance().findFarmCookingByID(id);
            this.session.user.getAvatarService().serverDialog("Món ăn "+farmcooking.getName()+" chưa được cập nhật");
        }
        else {
            FarmCooking farmcooking = PartManager.getInstance().findFarmCookingByID(id);
            if(this.session.user.Cooking.isEmpty()) {
                long Time = ((System.currentTimeMillis() / 60000) + farmcooking.getTime());
                ms = new Message(Cmd.COOKING);
                DataOutputStream ds = ms.writer();
                ds.writeShort(id);
                ds.writeShort(farmcooking.getTime());
                ds.flush();
                this.session.sendMessage(ms);
                Cooking newCooking = new Cooking(id,Time);
                this.session.user.Cooking.add(newCooking); // ghi món ăn vào database
                int require1 = farmcooking.getRequire1();
                int quantity1 = farmcooking.getQuantity1();
                int require2 = farmcooking.getRequire2();
                int quantity2 = farmcooking.getQuantity2();
                // Xử lý trừ nông sản trong kho
                if(require2 == -1 && quantity2 == -1) {
                    farmwarehouseitem fwi = PartManager.getInstance().findFarmwarehouseitemByID(require1);
                    if(require1 < 100) {
                        for(NongSan ns : this.session.user.NongSan) {
                            if(ns.getId() == fwi.getId()) {
                                int sanluong = ns.getSoluong() - quantity1;
                                ns.setSoluong(sanluong);
                            }
                        }
                    }
                    else {
                        for(NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                            if(nsdb.getId() == fwi.getId()) {
                                int sanluong = nsdb.getSoluong() - quantity1;
                                nsdb.setSoluong(sanluong);
                            }
                        }
                    }
                }
                else {
                    farmwarehouseitem fwi1 = PartManager.getInstance().findFarmwarehouseitemByID(require1);
                    farmwarehouseitem fwi2 = PartManager.getInstance().findFarmwarehouseitemByID(require2);
                    if(require1 < 100) {
                        for(NongSan ns : this.session.user.NongSan) {
                            if(ns.getId() == fwi1.getId()) {
                                int sanluong = ns.getSoluong() - quantity1;
                                ns.setSoluong(sanluong);
                            }
                        }
                    }
                    if(require2 < 100) {
                        for(NongSan ns : this.session.user.NongSan) {
                            if(ns.getId() == fwi2.getId()) {
                                int sanluong = ns.getSoluong() - quantity2;
                                ns.setSoluong(sanluong);
                            }
                        }
                    }
                    if(require1 > 100) {
                        for(NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                            if(nsdb.getId() == fwi1.getId()) {
                                int sanluong = nsdb.getSoluong() - quantity1;
                                nsdb.setSoluong(sanluong);
                            }
                        }
                    }
                    if(require2 > 100) {
                        for(NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                            if(nsdb.getId() == fwi2.getId()) {
                                int sanluong = nsdb.getSoluong() - quantity2;
                                nsdb.setSoluong(sanluong);
                            }
                        }
                    }
                    else {
                      System.out.println("ERROR COOKING");  
                    }
                }
                this.session.user.getAvatarService().serverDialog("Nấu " + farmcooking.getName()+ " thành công, vui lòng chờ trong " + farmcooking.getTime() + " phút");
                System.out.println("FOOD ID:" + id + " TIME: "+farmcooking.getTime());
            }
            else {
                this.session.user.getAvatarService().serverDialog("Món ăn hiện tại chưa hoàn thành. Không thể nấu thêm."); 
            }
        }
        
    }
    // Hoàn thành món ăn
     public void harvestCook(Message ms) throws IOException {
        if(this.session.user.Cooking.isEmpty()) {
            return;
        }
        else {
        ms = new Message(Cmd.HARVEST_COOK);
        DataOutputStream ds = ms.writer();
        for(Cooking cooking : this.session.user.Cooking) {
            ds.writeInt(cooking.getId());
            FarmCooking farmcooking = PartManager.getInstance().findFarmCookingByID(cooking.getId());
            int product = farmcooking.getProduct();
                boolean check = false;
                for(NongSanDacBiet nsdb : this.session.user.NongSanDacBiet) {
                    if(nsdb.getId() == product) {
                        int sanluongtang = nsdb.getSoluong() + 1;
                        nsdb.setSoluong(sanluongtang);
                        check = true;
                    }
                }
                if(!check) {
                    this.session.user.NongSanDacBiet.add(new NongSanDacBiet(product,1));
                }
        }
        ds.flush();
        this.session.sendMessage(ms);
        this.session.user.Cooking.remove(0);
        }
    }
    // Nấu nhanh
     public void fastCooking(Message ms) throws IOException {
        byte status = ms.reader().readByte();
        if(status == 0) {
        ms = new Message(Cmd.NAU_NHANH);
        DataOutputStream ds = ms.writer();
        ds.writeByte(0);
        ds.writeUTF("Bạn có muốn hoàn thành nhanh món ăn với giá 1 lượng");
        ds.flush();
        this.session.sendMessage(ms);
        }
        else {
            if(this.session.user.getLuong() < 1) {
                this.session.user.getAvatarService().serverDialog("Bạn không đủ 1 lượng"); 
            }
            else {
                ms = new Message(Cmd.NAU_NHANH);
                DataOutputStream ds = ms.writer();
                ds.writeByte(1);
                ds.writeInt(1); // 1 luong
                ds.writeInt(this.session.user.getLuong());
                ds.writeInt((this.session.user.getLuong() - 1));
                this.session.user.getAvatarService().updateMoney(1);
                for(Cooking cooking : this.session.user.Cooking) {
                    int time = (int)cooking.getTime();
                    cooking.setTime(-time);
                }
                this.session.user.getAvatarService().serverDialog("Món ăn đã hoàn thành");     
                ds.flush();
                this.session.sendMessage(ms);   
            }
        }
    }
    // Thu hoạch sản lượng vật nuôi
    public void harvestAnimal(Message ms) throws IOException {
        byte cell = ms.reader().readByte();
        byte cell2 = ms.reader().readByte();
        byte cell3 = ms.reader().readByte();
        byte cell4 = ms.reader().readByte();
        byte cell5 = ms.reader().readByte();
        //short number = ms.reader().readShort();
        ms = new Message(Cmd.HARVEST_ANIMAL);
        DataOutputStream ds = ms.writer();
        ds.writeByte(cell5);
        ds.writeShort((short)10);
        ds.flush();
        this.session.sendMessage(ms);   
        this.session.user.getAvatarService().serverDialog("Gặp lái buôn để thu hoạch"+cell5+"/"+cell2);     
    }
    // Nâng cấp chuồng trại
    public void updateFarmCattle(Message ms) throws IOException {
        byte type = ms.reader().readByte();
        int level = this.session.user.lvanimal;
        int price = (this.session.user.lvanimal + 1) * 500000;
        ms = new Message(Cmd.UPDATE_FARM_CATTLE);
        DataOutputStream ds = ms.writer();
         if (level == 10) {
            this.session.user.getAvatarService().serverDialog("Nông trại cá đã đạt cấp độ tối đa");
        return;
        }
         else if(type == 0) {
            ds.writeByte(0);
            ds.writeUTF("Bạn có muốn mở rộng nông trại lên cấp " + (level + 1) + " bằng " + price + " xu không?");
        }
        else {    
            ds.writeByte(1);
            ds.writeByte(0);
            ds.writeInt(0);
            this.session.user.lvanimal += 1;
            this.session.user.updateXu(-price);
            ds.writeInt((int)this.session.user.getXu());
            ds.writeInt((int)this.session.user.getLuong());
            ds.writeInt((int)this.session.user.getLuongKhoa());
        }
        ds.flush();
        this.session.sendMessage(ms);
        System.out.println(type);
    }
    

    // Nâng cấp hồ cá
    public void updateFarmFish(Message ms) throws IOException {
        byte type = ms.reader().readByte();
        int level = this.session.user.lvfish;
        int price = (this.session.user.lvfish + 1) * 100000;
        ms = new Message(Cmd.UPDATE_FARM_FISH);
        DataOutputStream ds = ms.writer();
         if (level == 10) {
            this.session.user.getAvatarService().serverDialog("Hồ cá đã đạt cấp độ tối đa");
        return;
        }
         else if(type == 0) {
            ds.writeByte(0);
            ds.writeUTF("Bạn có muốn mở rộng hồ cá lên cấp " + (level + 1) + " bằng " + price + " xu không?");
        }
        else {    
            ds.writeByte(1);
            ds.writeByte(0);
            ds.writeInt(0);
            this.session.user.lvfish += 1;
            this.session.user.updateXu(-price);
            ds.writeInt((int)this.session.user.getXu());
            ds.writeInt((int)this.session.user.getLuong());
            ds.writeInt((int)this.session.user.getLuongKhoa());
        }
        ds.flush();
        this.session.sendMessage(ms);
        System.out.println(type);
    }

    public void getInventory(Message ms) throws IOException {
    User us = session.user;
    //Vector<KeyValue<Integer, Integer>> nongsandacbiet = new Vector<>();
    //nongsandacbiet.add(new KeyValue(255, 20));// thit ca
    //nongsandacbiet.add(new KeyValue(215, 680));// khe
    //nongsandacbiet.add(new KeyValue(214, 4));// tinh dau huong duong
    ms = new Message(60);
    DataOutputStream ds = ms.writer();

    // Gửi số lượng hạt giống và thông tin
    ds.writeByte(this.session.user.hatgiong.size());
    for (HatGiong hatgiong : this.session.user.hatgiong) {
        ds.writeByte(hatgiong.getId());
        ds.writeShort(hatgiong.getSoluong());
        System.out.println("Sending HatGiong ID: " + hatgiong.getId() + ", Quantity: " + hatgiong.getSoluong());
    }

    // Gửi số lượng nông sản và thông tin
    ds.writeByte(this.session.user.NongSan.size());
    for (NongSan ns : this.session.user.NongSan) {
        ds.writeByte(ns.getId());
        ds.writeShort(ns.getSoluong());
        System.out.println("Sending NongSan ID: " + ns.getId() + ", Quantity: " + ns.getSoluong());
    }

    // Gửi số dư xu và level
    ds.writeInt(Math.toIntExact(this.session.user.getXu()));
    ds.writeByte(us.getLeverFarm());
    ds.writeByte(us.getLeverPercen());
    System.out.println("Sending Xu: " + this.session.user.getXu() + ", LevelFarm: " + us.getLeverFarm() + ", LevelPercen: " + us.getLeverPercen());

    // Gửi số lượng phân bón và thông tin
    ds.writeByte(this.session.user.PhanBon.size());
    for (PhanBon pb : this.session.user.PhanBon) {
        ds.writeShort(pb.getId());
        ds.writeShort(pb.getSoluong());
        System.out.println("Sending PhanBon ID: " + pb.getId() + ", Quantity: " + pb.getSoluong());
    }
    // Gửi nông sản đặc biệt
    ds.writeByte(this.session.user.NongSanDacBiet.size());
        for(NongSanDacBiet nsdb1 : this.session.user.NongSanDacBiet) {
            ds.writeShort(nsdb1.getId());
            ds.writeShort(nsdb1.getSoluong());
            System.out.println("Sending NongSan ID: " + nsdb1.getId() + ", Quantity: " + nsdb1.getSoluong());
        }

    // Gửi thông tin bổ sung
    ds.writeByte(1);  // Nếu đây là một thông tin cố định, bạn có thể giữ lại
    ds.writeInt(64000); // Cũng giữ lại nếu đây là thông tin cần thiết
    ds.writeBoolean(true); // Giữ lại nếu cần thiết
    ds.writeShort(us.getLeverFarm());
    ds.writeByte(us.getLeverPercen());
    System.out.println("Sending additional info: LevelFarm: " + us.getLeverFarm() + ", LevelPercen: " + us.getLeverPercen());

    // Gửi số lượng nông sản và thông tin
    ds.writeByte(this.session.user.NongSan.size());
    for (NongSan ns : this.session.user.NongSan) {
        ds.writeShort(ns.getId());
        ds.writeInt(ns.getSoluong());
        System.out.println("Sending NongSan ID: " + ns.getId() + ", Quantity: " + ns.getSoluong());
    }
    // Gửi lại thông tin nông sản đặc biệt một lần nữa
    ds.writeByte(this.session.user.NongSanDacBiet.size());
        for(NongSanDacBiet nsdb1 : this.session.user.NongSanDacBiet) {
            ds.writeShort(nsdb1.getId());
            ds.writeInt(nsdb1.getSoluong());
            System.out.println("Sending NongSan ID: " + nsdb1.getId() + ", Quantity: " + nsdb1.getSoluong());
        }
        //ds.writeShort(214);
        //ds.writeInt(4);
    // Kết thúc và gửi thông điệp
    ds.flush();
    this.session.sendMessage(ms);
}



    private void writeInfoCell(DataOutputStream ds, LandItem land) throws IOException {
        ds.writeShort((int)land.getMinutesSincePlanted());
        ds.writeByte(land.getSucKhoe());
        ds.writeByte(1); //100 la héo
        ds.writeBoolean(land.isWatered());
        ds.writeBoolean(land.isFertilized());
        ds.writeBoolean(land.isHarvestable());
    }

    private void writeInfoAnimal(DataOutputStream ds, Animal animal) throws IOException {
        //ds.writeInt(animal.getHealth());
        //ds.writeByte(animal.getLevel());
        //ds.writeByte(animal.getResourceCount());
        //ds.writeByte(animal.getNextProductionTime());
        //ds.writeBoolean(animal.isAlive());
       // ds.writeBoolean(animal.isReadyForBreeding());
        //ds.writeBoolean(animal.isHarvestable());
        //ds.writeByte(1); // số lượng đv
        //ds.writeByte(50); // id con ga
        //ds.writeInt(1000); // thoi gian trương thanh
        //ds.writeByte(100); // hp
        //ds.writeByte(10); // tien do thu hoach 10%
        //ds.writeByte(1); // san luong trung, sua
        //ds.writeBoolean(false); // Không đói
        //ds.writeBoolean(false); // Không bị bệnh loại 1
        //ds.writeBoolean(false); // Không bị bệnh loại 2
    }


    public void joinFarm(Message ms) throws IOException, SQLException {
        int userId = ms.reader().readInt();
        ms = new Message(Cmd.JOIN);
        DataOutputStream ds = ms.writer();
        ds.writeInt(userId);


        int landSize = this.session.user.landItems.size();
        ds.writeByte(landSize);  // Ghi số lượng ô đất

        // Ghi thông tin các ô đất (cây)
        for (int i = 0; i < landSize; ++i) {
            LandItem landItem =  this.session.user.landItems.get(i);
            if (landItem.getType() >= 0) {
                ds.writeByte(landItem.getType());  //id
                writeInfoCell(ds, landItem);
            } else {
                ds.writeByte(-1);  // Không có cây trong ô đất này
            }
        }
        // Code vật nuôi
        ds.writeByte((byte) this.session.user.Animal.size());  // Ghi số lượng động vật
        for (Animal animal : this.session.user.Animal) {
            long bornTime = animal.getBornTime(); // bornTime là long, không phải int
            long currentTime = System.currentTimeMillis();
            // Tính growTime bằng cách lấy chênh lệch thời gian hiện tại và thời gian sinh
            int growTime = (int) ((currentTime - bornTime) / 60000); // Chỉ lấy phần nguyên phút  
        // Nếu thời gian trưởng thành nhỏ hơn 1 phút, thì set growTime là 1 phút
            if (growTime < 1) {
                animal.setGrowTime(1);
            } else {
                animal.setGrowTime(growTime);
            }
            // Tính tiến độ thu hoạch
            int id = (int)animal.getId();
            FarmAnimal farmanimal = PartManager.getInstance().findFarmAnimalByID(id);
            if(animal.getGrowTime() >= farmanimal.getHarvesttime()) {
                if(animal.getHarvestProgress() <= animal.getGrowTime() || animal.getHarvestProgress() == 1)
                {
                    animal.setQuantity(20);
                }
                else
                {
                    animal.setQuantity(0);
                }
            }
            else {
                animal.setQuantity(-1);
            }
            // Ghi thông tin động vật vào DataOutputStream
            ds.writeByte(animal.getId());  // Ghi ID động vật
            ds.writeInt(animal.getGrowTime()); // Thời gian trưởng thành
            ds.writeByte(animal.getHealth()); // Sức khỏe (HP)
            ds.writeByte(animal.getHarvestProgress()); // Tiến độ thu hoạch
            ds.writeByte(animal.getQuantity()); // Sản lượng (trứng, sữa...)
            ds.writeBoolean(animal.isAlive()); // Động vật còn sống
            ds.writeBoolean(animal.isHungry()); // Động vật có đói không
            ds.writeBoolean(animal.isSick()); // Động vật có bệnh không
}

        // Code test động vật
        //ds.writeByte(1); // số lượng đv
        //ds.writeByte(50); // id con ga
        //ds.writeInt(1000); // thoi gian trương thanh
        //ds.writeByte(100); // hp
        //ds.writeByte(10); // tien do thu hoach 10%
        //ds.writeByte(1); // san luong trung, sua
        //ds.writeBoolean(false); // Không đói
        //ds.writeBoolean(false); // Không bị bệnh loại 1
        //ds.writeBoolean(false); // Không bị bệnh loại 2

        // Ghi thông tin khác
        ds.writeByte(this.session.user.lvanimal);// chỉnh kích cỡ chuồng bò
        ds.writeByte(this.session.user.lvfish);//chỉnh kích cỡ hồ cá
        // Xử lý cây khế
        for(Starfruil sf : this.session.user.starfruil) {   
             LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(sf.getPlantedTime()) || now.isEqual(sf.getPlantedTime())) {
            sf.setQuantity(30);
        }
        ds.writeShort(sf.getLevel());  //cấp độ cây khế
        ds.writeShort(43);//43//img cay khe
        if(sf.getQuantity() > 0) {
            ds.writeShort(46);//46//img qua khe chỉnh theo lv
        }
        else {
            ds.writeShort(-1);//46//img qua khe
        }
        ds.writeShort(sf.getQuantity());  // Số khế có thể thu hoạch
        ds.writeShort(0);// chưa biết
        ds.writeShort(0);// chưa biết
        long secondsRemaining = Duration.between(now, sf.getTimeToUpgrade()).getSeconds();
        if (now.isAfter(sf.getTimeToUpgrade()) || now.isEqual(sf.getTimeToUpgrade())) {
            if(sf.getTimeToHarvest() == 1) {
                sf.setTimeToHarvest(-1);
                int tangcap = sf.getLevel() + 1;
                sf.setLevel(tangcap);
            }
            ds.writeShort(0);
        } else {
            ds.writeShort((int)secondsRemaining/60);// thời gian nâng cấp khế 
        }
        }
        // Ghi cấp độ các ô đất
        for (int i = 0; i < landSize; ++i) {
            LandItem landItem =  this.session.user.landItems.get(i);
            ds.writeByte(landItem.getlevel());  // Trạng thái ô đất
        }
        //nấu ăn
        if (this.session.user.Cooking.isEmpty()) {
            ds.writeShort(0);
            ds.writeShort(0);
        } else {
            for(Cooking cooking : this.session.user.Cooking) {
                long time = cooking.getTime();
                long current = (System.currentTimeMillis() / 60000);
                int require = (int)(time - current);
                System.out.println("TIME COOKING: "+require);
                ds.writeShort(cooking.getId());
                if (require <= 0) {
                    ds.writeShort(0);
                }
                else {
                    ds.writeShort(require);
                }
            }
        }            
        ds.flush();
        this.session.sendMessage(ms);
        
    }


    public void getImgFarm(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = session.getResourcesPath() + "farm/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.GET_IMG_FARM);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.write(dat);
        ds.flush();
        this.session.sendMessage(ms);
    }
}
