package avatar.service;

import avatar.constants.Cmd;
import avatar.convert.ItemConverter;
import avatar.db.DbManager;
import avatar.item.Item;
import avatar.model.User;
import avatar.network.Message;
import avatar.network.Session;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import avatar.play.Map;
import avatar.server.UserManager;
import avatar.server.Utils;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Service {

    private static final Logger logger = Logger.getLogger(Service.class);
    protected Session session;

    public Service(Session cl) {
        this.session = cl;
    }

    public void removeItem(int userID, short itemID) {
        try {
            Message ms = new Message(Cmd.REMOVE_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeShort(itemID);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("removeItem() ", ex);
        }


    }

    public void serverDialog(String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTextBoxPopup(int userId, int menuId, String message, int type) {
        try {
            Message ms = new Message(Cmd.TEXT_BOX);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeByte(menuId);
            ds.writeUTF(message);
            ds.writeByte(type);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverMessage(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("serverMessage ", e);
        }
    }

    public void serverInfo(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("serverMessage ", e);
        }
    }

    public void weather(byte weather) {
        try {
            System.out.println("weather: " + weather);
            Message ms = new Message(Cmd.WEATHER);
            DataOutputStream ds = ms.writer();
            ds.writeByte(weather);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("weather() ", ex);
        }
    }
    // Lấy data serer
    public List<User> getTopdameboss() {
        List<User> topMoney = new ArrayList<>();

        String sql = "SELECT u.username, p.xu_from_boss, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.xu_from_boss DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int xu_from_boss = rs.getInt("xu_from_boss");
                String topmoney = String.valueOf("lượt click: " + xu_from_boss);

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Money: " + topmoney + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, topmoney, wearings); // Tạo đối tượng User
                topMoney.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topMoney; // Trả về danh sách người chơi
    }
    public List<User> getTopuseCoin() {
        List<User> topMoney = new ArrayList<>();

        String sql = "SELECT u.username, p.useCoin, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.useCoin DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int useCoin = rs.getInt("useCoin");
                String topmoney = String.valueOf("Xu đã dùng: " + useCoin);

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Money: " + topmoney + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, topmoney, wearings); // Tạo đối tượng User
                topMoney.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topMoney; // Trả về danh sách người chơi
    }
    public List<User> getTopuseGold() {
        List<User> topMoney = new ArrayList<>();

        String sql = "SELECT u.username, p.useGold, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.useGold DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int useGold = rs.getInt("useGold");
                String topmoney = String.valueOf("Lượng đã sử dụng : " + useGold);

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Money: " + topmoney + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, topmoney, wearings); // Tạo đối tượng User
                topMoney.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topMoney; // Trả về danh sách người chơi
    }
    public List<User> getTopLv() {
        List<User> topLv = new ArrayList<>();

        String sql = "SELECT u.username, p.exp_main, p.level_main, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.level_main DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int lv = rs.getInt("level_main");
                int exp = rs.getInt("exp_main");
                int expmax = (lv * (lv + 1) / 2) * 1000;
                int percent = (exp * 100 / expmax);
                String toplv = "Lv: "+lv+"+"+percent+"%";

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Lv: " + toplv + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, toplv, wearings); // Tạo đối tượng User
                topLv.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topLv; // Trả về danh sách người chơi
    }
    public List<User> getTopMoney() {
        List<User> topMoney = new ArrayList<>();

        String sql = "SELECT u.username, p.xu, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.xu DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int xu = rs.getInt("xu");
                String topmoney = String.valueOf("Xu: " + xu);

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Money: " + topmoney + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, topmoney, wearings); // Tạo đối tượng User
                topMoney.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topMoney; // Trả về danh sách người chơi
    }
    public List<User> getTopMoneygold() {
        List<User> topMoney = new ArrayList<>();

        String sql = "SELECT u.username, p.luong, p.wearing " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.luong DESC " +
                "LIMIT 10";

        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int luong = rs.getInt("luong");
                String topmoney = String.valueOf("Lượng: " + luong);

                // Lấy dữ liệu mảng "wearing" từ cơ sở dữ liệu
                List<Integer> wearings = new ArrayList<>();
                JSONArray wearing = (JSONArray) JSONValue.parse(rs.getString("wearing"));

                // Lặp qua mảng JSON, chỉ lấy 'id' và thêm vào danh sách
                for (Object o : wearing) {
                    JSONObject obj = (JSONObject) o;
                    int id = ((Long) obj.get("id")).intValue(); // Lấy id
                    wearings.add(id); // Thêm id vào danh sách
                }

                // Chuyển danh sách "wearings" thành chuỗi cách nhau bằng dấu phẩy
                String wearingIds = String.join(", ", wearings.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));

                // In ra thông tin (Username, Top Phao Luong, and Wearing IDs)
                System.out.println("Username: " + username + ", Top Money: " + topmoney + ", Wearing IDs: " + wearingIds);

                // Tạo đối tượng User với các thông tin đã xử lý
                User player = new User(username, topmoney, wearings); // Tạo đối tượng User
                topMoney.add(player); // Thêm vào danh sách
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Xử lý ngoại lệ khi truy vấn thất bại
        }

        return topMoney; // Trả về danh sách người chơi
    }
    public void BxhLv (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top cao thủ"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    public void BxhenterBoss (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top cao thủ lượt đánh boss"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    public void BxhMoney (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top đại gia Xu"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    public void BxhMoneyGold (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top đại gia Lượng"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    public void BxhuseCoin (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top sài xu"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    public void BxhuseGold (List<User> topPlayers) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bảng xếp hạng");

            // Thông tin chung
            dos.writeUTF("Top sài lượng"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(topPlayers.size());
            for (User user : topPlayers) {
                dos.writeByte(user.getLstitemID().size()); // Số phần mặc
                for (int id : user.getLstitemID()) {
                    dos.writeShort(id); // ID item
                }

                dos.writeInt(user.getId());
                dos.writeShort(user.getIdImg());
                dos.writeUTF(user.getUsername());
                dos.writeUTF(user.getTop());
            }

            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }
    // bạn bè
    public void listFriend (List<User> friends) {
        try {
            Message msg = new Message(Cmd.CUSTOM_LIST);
            DataOutputStream dos = msg.writer();

            // Tên danh sách
            dos.writeUTF("Bạn bè");

            // Thông tin chung
            dos.writeUTF("Bạn bè"); // Tiêu đề
            dos.writeInt(session.user.getId());  // ID người xem
            dos.writeByte(3);   // Type = 3 (ranking list)
            dos.writeByte(0);   // Page đầu tiên

            dos.writeShort(10); // tổng số bạn bè
            for (int i = 0; i<10; i++) {
                dos.writeByte(1); // Số phần mặc

                dos.writeShort(2693); // ID item
                dos.writeInt(0); // id nhân vật
                dos.writeShort(-1); // ảnh clan
                dos.writeUTF(""); // tên
                dos.writeUTF(""); // nội dung mô tả
            }


            // Menu options
            dos.writeByte(2); // Số lượng menu
            dos.writeByte(1);
            dos.writeUTF("Xem thông tin");
            dos.writeByte(2);
            dos.writeUTF("Tố Cáo");

            session.sendMessage(msg);
        } catch (Exception e) {
            logger.error("Error showing ranking list", e);
        }
    }



    public String DuDoanNY(User us){

        List<User> lstUs = UserManager.users;
        String result = "";
        byte gender = us.getGender();
        int randomIndex = Utils.nextInt(lstUs.size());
        User ulove = lstUs.get(randomIndex);
        String map = checkNameMap(ulove.getZone().getMap());
        if(gender == 1){
            if(ulove.getGender() == gender){
                result = ulove.getUsername() + " (cú có gai) đang ở" +
                        " Map : " + map + " Khu :" + ulove.getZone().getId();
            }else
                result = ulove.getUsername() + " (girl) đang ở" +
                        " Map : " + map + " Khu :" + ulove.getZone().getId();
        }else
        {
            if(ulove.getGender() == gender){
                result = ulove.getUsername() + "(Gái đó : v) đang ở" +
                        " Map : " + map + " Khu :" + ulove.getZone().getId();
            }else
                result = ulove.getUsername() + " (boy nè) đang ở" +
                        " Map : " + map + " Khu :" + ulove.getZone().getId();
        }
        return result;
    }

    public String checkNameMap(Map m){
        int mapid = m.getId();
        String Map = "";
        switch (mapid) {
            case 0:
                Map = "Khu Mặt trời";
                break;
            case 1:
                Map = "Khu quay số cũ";
                break;
            case 2:
                Map = "Khu đấu giá cũ";
                break;
            case 3:
                Map = "Khu ăn xin trái";
                break;
            case 4:
                Map = "Khu cưới , clan";
                break;
            case 5:
                Map = "Khu ăn xin phải";
                break;
            case 6:
                Map = "Khu cô giáo";
                break;
            case 7:
                Map = "Khu dưới cô giáo";
                break;
            case 9:
                Map = "Khu giải trí";
                break;
            case 10:
                Map = "Khu lễ đường";
                break;
            case 11:
                Map = "Bến xe công viên";
                break;
            case 13:
                Map = "Khu sinh thái";
                break;
            case 14:
                Map = "Khu câu cá rô";
                break;
            case 15:
                Map = "Khu câu cá lóc";
                break;
            case 16:
                Map = "Khu câu cá mập";
                break;
            case 17:
                Map = "Khu ngoại ô";
                break;
            case 18:
                Map = "Trong nhà tù";
                break;
            case 19:
                Map = "Trong lễ đường";
                break;
            case 23:
                Map = "Khu mua sắm";
                break;
        }
        return Map;
    }


    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }
}
