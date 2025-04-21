package avatar.service;

import avatar.db.DbManager;
import java.io.IOException;
import java.util.Random;
import avatar.play.Zone;
import avatar.network.Message;
import avatar.network.Session;

import java.io.DataOutputStream;
import org.apache.log4j.Logger;
import java.util.Timer;
import java.util.TimerTask;
import avatar.race.PetInfo;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import avatar.race.RaceHistoryEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RaceService extends Service {
    private static final int NUM_PETS = 6;
    private final Random random;
    private static final Logger logger = Logger.getLogger(RaceService.class);
    private static final int MAX_HISTORY = 10;
    private static final List<RaceHistoryEntry> raceHistory = new ArrayList<>();
    private static final int[] ALLOWED_PETS = {100, 500, 1000, 2000, 5000, 10000, 20000, 30000, 50000};

    public RaceService(Session client) {
        super(client);
        this.random = new Random();
    }

    public void handleJoinRace(Message msg) {
        try(Connection connection = DbManager.getInstance().getConnection(); 
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `race` WHERE `id` = 1;");
            ResultSet res = ps.executeQuery();) {
                while (res.next()) {
                    int status = res.getInt("status");
                    System.out.println(status);
                    if(status == -1) {
                        startRaceCountdown();
                    }
                    else {
                        try {
            //if (session.user != null) {
                //Zone currentZone = session.user.getZone();
                //if (currentZone != null) {
                    //currentZone.leave(session.user);
                //}
            //}

            // 1. Gửi thông tin khởi tạo race
            Message initMsg = new Message(1); 
            DataOutputStream ds = initMsg.writer();
            ds.writeByte(0); // b3 = 0 - Khởi tạo race
            
            // Gửi thông tin 6 pet đua
            for (int i = 0; i < NUM_PETS; i++) {
                ds.writeByte(i); // IDDB
                ds.writeByte(generateRate(1)); // Tỉ lệ cược
                ds.writeShort(generatePetImage(i)); // ID hình ảnh pet
                ds.writeShort(generatePetIcon(i)); // ID icon pet
            }
            ds.writeShort(0); // Thời gian chờ 30s
            ds.flush();
            sendMessage(initMsg);

            // Bắt đầu đếm ngược 30s
            Timer waitTimer = new Timer();
            waitTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startRaceCountdown(); // Sau 30s sẽ bắt đầu đếm ngược 3s
                }
            }, 30000); // 300000ms = 30s

        } catch (IOException e) {
            logger.error("handleJoinRace() ", e);
        }
                    }
                }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        
    }

    // Gọi sau khi đếm ngược 300s
    private void startRaceCountdown() {
        try {
            System.out.println("=== START COUNTDOWN ===");
           
            // 2. Gửi message bắt đầu đếm ngược 3s
            Message countdownMsg = new Message(1);
            DataOutputStream ds = countdownMsg.writer();
            ds.writeByte(1); 
            ds.writeBoolean(false);
            ds.writeShort(3);
            ds.writeLong(System.currentTimeMillis());
            ds.flush();
            sendMessage(countdownMsg);
            System.out.println("Đã gửi message countdown");
            
  

            // 3. Schedule để bắt đầu đua sau 3s
            Timer raceTimer = new Timer();
            System.out.println("Bắt đầu đếm ngược 3s...");
            raceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("=== TIMER TRIGGERED ===");
                        startRace();
                        System.out.println("=== RACE STARTED ===");
                    } catch (Exception e) {
                        System.out.println("Lỗi trong timer task: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        raceTimer.cancel();
                    }
                }
            }, 3000);

        } catch (IOException e) {
            System.out.println("Lỗi trong startRaceCountdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void startRace() {
        try {
            // Trừ tiền cược của tất cả người chơi trước khi bắt đầu đua
            if (session.user.getPets() != null && !session.user.getPets().isEmpty()) {
                int totalPet = session.user.getTotalPetAmount();
                //session.user.updateXu(-totalBet);
            }
            
            System.out.println("=== STARTING RACE LOGIC ===");
            
            Message raceMsg = new Message(1);
            DataOutputStream ds = raceMsg.writer();
            ds.writeByte(1); 
            ds.writeBoolean(true);
            
            final int FINISH_LINE = 2700; 
            int[] petDistances = new int[NUM_PETS];  
            double[] petRealPositions = new double[NUM_PETS];
            double[] finishTimes = new double[NUM_PETS];
            int winningPet = -1;
            double firstFinishTime = Double.MAX_VALUE;
            int maxSpeed = 0;
                
            // Chọn ngẫu nhiên một pet sẽ về đích
            int guaranteedPet = random.nextInt(NUM_PETS);
            System.out.println("Guaranteed pet to finish: " + guaranteedPet);
            
            for (int i = 0; i < NUM_PETS; i++) {
                byte numPhases = 5;
                ds.writeByte(numPhases);
                
                int totalDistance = 0;
                double timePosition = 0;
                boolean reachedFinish = false;
                
                for (int j = 0; j < numPhases; j++) {
                    if (!reachedFinish) {
                        short distance;
                        short speed;
                        
                        if (i == guaranteedPet && j == numPhases - 1 && timePosition < FINISH_LINE) {
                            // Đảm bảo pet được chọn sẽ về đích trong phase cuối
                            double remainingDistance = FINISH_LINE - timePosition;
                            speed = (short)(4 + random.nextInt(4));
                            distance = (short)Math.ceil(remainingDistance / speed);
                        } else {
                            distance = (short)(90 + random.nextInt(5));
                            speed = (short)(4 + random.nextInt(4));
                        }
                        
                        totalDistance += distance;
                        timePosition += distance * speed;
                        
                        if (timePosition >= FINISH_LINE) {
                            reachedFinish = true;
                            double excess = timePosition - FINISH_LINE;
                            timePosition = FINISH_LINE;
                            distance = (short)(distance - (excess/speed));
                            
                            finishTimes[i] = j + (distance / (double)speed);
                            
                            if (finishTimes[i] < firstFinishTime) {
                                firstFinishTime = finishTimes[i];
                                winningPet = i;
                                maxSpeed = speed;
                            }
                            else if (finishTimes[i] == firstFinishTime && speed > maxSpeed) {
                                winningPet = i;
                                maxSpeed = speed;
                            }
                            
                            System.out.println("Pet " + i + " finished at time: " + finishTimes[i] + " with speed: " + speed);
                        }
                        
                        ds.writeShort(distance);
                        ds.writeShort(speed);
                    } else {
                        ds.writeShort(0);
                        ds.writeShort(0);
                    }
                }
                
                petDistances[i] = totalDistance;
                petRealPositions[i] = timePosition;
            }
            
            System.out.println("\n=== RACE POSITIONS ===");
            for (int i = 0; i < NUM_PETS; i++) {
                System.out.println("Pet " + i + ": Distance=" + petDistances[i] + 
                                 ", Real Position=" + petRealPositions[i]);
            }
            System.out.println("Winner is Pet " + winningPet + 
                             " with final position: " + petRealPositions[winningPet]);
            
            // Debug thông tin trước khi gửi kết quả
            System.out.println("\n=== RACE RESULTS ===");
            System.out.println("First Finish Time: " + firstFinishTime);
            System.out.println("Max Speed: " + maxSpeed);
            System.out.println("Winning Pet: " + winningPet);
            
          
            ds.writeShort(10);
            ds.writeShort(10);
            ds.writeLong(System.currentTimeMillis());
            ds.flush();
            sendMessage(raceMsg);
            System.out.println("Đã gửi message race");
            
            sendWinningResult((byte)winningPet);
            System.out.println("Đã gửi kết quả winner: Pet " + winningPet);
                
        } catch (Exception e) {
            System.out.println("Lỗi trong startRace: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendWinningResult(byte winningPetId) {
        try {
            Message winMsg = new Message(10);
            DataOutputStream ds = winMsg.writer();
            
            ds.writeByte(winningPetId);
            ds.writeUTF(getPetName(winningPetId));
            ds.writeByte(generateRate(winningPetId));
            
            // Lấy thông tin cược của người chơi cho pet hiện tại
            PetInfo currentBet = session.user.getPets().get(winningPetId);
            
            if (currentBet != null) {
                int betMoney = currentBet.getAmount();  // Tiền đặt cược
                int rate = currentBet.getRate();        // Tỉ lệ cược
                
                if (winningPetId == currentBet.getPetId()) {  // Nếu pet này thắng
                    int winMoney = betMoney * rate;         // Tiền thắng = tiền cược * tỉ lệ
                    int tax = (int)(winMoney * 0.05);      // Thuế 5% từ tiền thắng
                    int actualMoney = winMoney - tax; // Tiền thực nhận
                    
                    ds.writeInt(betMoney);    // Tiền đặt cược
                    ds.writeInt(winMoney);    // Tiền thắng
                    ds.writeInt(tax);         // Tiền thuế
                    ds.writeInt(actualMoney); // Tiền thực nhận
                    
                    // Cộng tiền thắng
                   // session.user.updateXu(actualMoney);
                  
                    
                    System.out.println("User " + session.user.getId() + " won " + actualMoney + 
                                     " xu from pet " + winningPetId);
                } else {  // Nếu pet này thua
                    ds.writeInt(betMoney);    // Vẫn hiển thị số tiền đã đặt
                    ds.writeInt(0);           // Không có tiền thắng
                    ds.writeInt(0);           // Không có thuế
                    ds.writeInt(0);           // Không có tiền thực nhận
                }
            } else {  // Nếu không có cược cho pet này
                ds.writeInt(0);
                ds.writeInt(0);
                ds.writeInt(0);
                ds.writeInt(0);
            }
            
            ds.flush();
            sendMessage(winMsg);
           
           
            
            // Thêm vào lịch sử sau khi xác định người thắng
            synchronized(raceHistory) {
                raceHistory.add(0, new RaceHistoryEntry((short)(1066 + winningPetId), System.currentTimeMillis()));
                if (raceHistory.size() > MAX_HISTORY) {
                    raceHistory.remove(raceHistory.size() - 1);
                }
            }
             // Reset cược sau khi đua xong
             session.user.clearPets();

            // Schedule new race after 10 seconds
            Timer newRaceTimer = new Timer();
            newRaceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println("=== STARTING NEW RACE ===");
                        handleJoinRace(new Message(-1)); // -1 as dummy message ID
                    } catch (Exception e) {
                        logger.error("Error starting new race: ", e);
                    } finally {
                        newRaceTimer.cancel();
                    }
                }
            }, 40000); // 10 seconds delay

        } catch (IOException e) {
            logger.error("sendWinningResult() ", e);
        }
    }

    
    

    public void getPetInfo(byte petId) throws IOException {
        Message response = new Message(2);
        DataOutputStream ds = response.writer();

        try {
            ds.writeShort(generatePetImageInfo(petId));
            ds.writeUTF(getPetName(petId));
            ds.writeShort(generateWinCount());
            ds.writeByte(generateRate(petId));
            ds.writeByte(generatePhongDo(petId));
            ds.writeByte(generateHealth(petId));
            ds.flush();
            sendMessage(response);
        } catch (IOException e) {
            logger.error("getPetInfo() ", e);
        }
    }

    public void handleBetting(Message msg) throws IOException {
        try {
            byte petId = msg.reader().readByte();
            int money = msg.reader().readInt();
            
            // Validate cơ bản
            if (!validateBet(petId, money)) {
                session.user.getService().serverDialog("Số tiền cược không hợp lệ!");
                return;
            }

            // Kiểm tra giới hạn cược tối thiểu
            if (money < 1000) {
                session.user.getService().serverDialog("Số tiền cược tối thiểu là 1,000 xu!");
                return;
            }

            // Kiểm tra số xu với TỔNG số tiền đã đặt + tiền muốn đặt mới
            int totalCurrentBets = session.user.getTotalPetAmount(); // Tổng tiền đã đặt
            int newTotalBet = totalCurrentBets + money; // Tổng sau khi đặt thêm
            
            if (session.user.getXu() < newTotalBet) {
                session.user.getService().serverDialog("Bạn không đủ xu để đặt cược! Tổng cược vượt quá số xu hiện có.");
                return;
            }

            // Kiểm tra giới hạn cược cho mỗi pet
            PetInfo existingBet = session.user.getPets().get(petId);
            int totalPetBet = (existingBet != null ? existingBet.getAmount() : 0) + money;
            if (totalPetBet > 500000000) {
                session.user.getService().serverDialog("Tổng tiền cược cho mỗi pet không được vượt quá 500,000,000 xu!");
                return;
            }

            // Kiểm tra tổng tiền cược không vượt quá 1 tỷ
            if (newTotalBet > 1000000000) {
                session.user.getService().serverDialog("Tổng tiền cược không được vượt quá 1,000,000,000 xu!");
                return;
            }

            // Lưu thông tin cược
            int rate = generateRate(petId);
            PetInfo newPet = new PetInfo(petId, money, rate);
            session.user.addPet(newPet);

            // Gửi response
            Message response = new Message(5);
            DataOutputStream ds = response.writer();
            ds.writeByte(petId);
            ds.writeInt(money);
            ds.writeInt(rate);
            ds.flush();
            sendMessage(response);

        } catch (IOException e) {
            logger.error("handleBetting() ", e);
        }
    }

    private boolean validateBet(byte petId, int money) {
        // Check valid pet ID and positive amount
        if (petId < 0 || petId >= NUM_PETS || money <= 0) {
            return false;
        }
        
        // Check if bet amount matches allowed values
        for (int allowedBet : ALLOWED_PETS) {
            if (money == allowedBet) {
                return true;
            }
        }
        return false;
    }

    private String getPetName(byte petId) {
        switch (petId) {
            case 0: return "Khủng Long";
            case 1: return "Chó";
            case 2: return "Vẹt"; 
            case 3: return "Mèo";
            case 4: return "Hươu";
            case 5: return "Pikachu";
            default: return "Pet " + (petId + 1);
        }
    }

    private byte generateRate(int petId) {
        byte[] rates = {1, 2, 3, 4, 5, 7}; // Tỉ lệ cược cho mỗi pet: 1:1, 1:2, 1:3, 1:4, 1:5, 1:7
        return rates[petId];
    }

    private byte generatePhongDo(int petId) {
        // Phong độ: 2 = Tốt, 1 = Bình thường, 0 = Kém
        byte[] phongDo;
        switch(generateRate(petId)) {
            case 1: // Tỉ lệ 1:1
                phongDo = new byte[]{2, 2, 2}; // 100% tốt
                break;
            case 2: // Tỉ lệ 1:2
                phongDo = new byte[]{1, 2, 2}; // 66% tốt, 33% bình thường
                break;
            case 3: // Tỉ lệ 1:3
                phongDo = new byte[]{1, 1, 2}; // 33% tốt, 66% bình thường
                break;
            case 4: // Tỉ lệ 1:4
                phongDo = new byte[]{0, 1, 2}; // 33% mỗi loại
                break;
            case 5: // Tỉ lệ 1:5
                phongDo = new byte[]{0, 0, 1}; // 66% kém, 33% bình thường
                break;
            case 7: // T lệ 1:7
                phongDo = new byte[]{0, 0, 0}; // 100% kém
                break;
            default:
                phongDo = new byte[]{1, 1, 1}; // Mặc định bình thường
        }
        return phongDo[random.nextInt(phongDo.length)];
    }

    private byte generateHealth(int petId) {
        // Sức khỏe: 2 = Tốt, 1 = Bình thường, 0 = Yếu
        byte[] health;
        switch(generateRate(petId)) {
            case 1: // Tỉ lệ 1:1
                health = new byte[]{2, 2, 2}; // 100% tốt
                break;
            case 2: // Tỉ lệ 1:2
                health = new byte[]{1, 2, 2}; // 66% tốt, 33% bình thường
                break;
            case 3: // Tỉ lệ 1:3
                health = new byte[]{1, 1, 2}; // 33% tốt, 66% bình thường
                break;
            case 4: // Tỉ lệ 1:4
                health = new byte[]{0, 1, 2}; // 33% mỗi loại
                break;
            case 5: // Tỉ lệ 1:5
                health = new byte[]{0, 0, 1}; // 66% yếu, 33% bình thường
                break;
            case 7: // Tỉ lệ 1:7
                health = new byte[]{0, 0, 0}; // 100% yếu
                break;
            default:
                health = new byte[]{1, 1, 1}; // Mặc định bình thường
        }
        return health[random.nextInt(health.length)];
    }

    private short generatePetImageInfo(int index) {
        return (short)(1076 + index);
    }
    private short generatePetImage(int index) {
        return (short)(1056 + index);
    }

    private short generatePetIcon(int index) {
        return (short)(1066 + index);
    }
    
    private short generateWinCount() {
        return (short)(random.nextInt(50) + 1);
    }
    
    public void handleChat(Message msg) throws IOException {
        try {
            String text = msg.reader().readUTF();
            
            // Broadcast chat message to all users in race
            Message response = new Message(9);
            DataOutputStream ds = response.writer();
            ds.writeUTF(text);
            ds.flush();
            sendMessage(response);
            
        } catch (IOException e) {
            logger.error("handleChat() ", e);
        }
    }
    public void sendRaceHistory() {
        try {
            Message historyMsg = new Message(8);
            DataOutputStream ds = historyMsg.writer();
            
            synchronized(raceHistory) {
                ds.writeByte(raceHistory.size());
                for (RaceHistoryEntry entry : raceHistory) {
                    ds.writeShort(entry.getPetId());
                    
                    // Format thời gian từ timestamp
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
                    String timeStr = sdf.format(new Date(entry.getRaceTime()));
                    ds.writeUTF(timeStr);
                }
            }
            
            ds.flush();
            sendMessage(historyMsg);
        } catch (IOException e) {
            logger.error("sendRaceHistory() ", e);
        }
    }
    
}