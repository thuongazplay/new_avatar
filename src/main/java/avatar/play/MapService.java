
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.play;

import avatar.item.Item;
import avatar.constants.Cmd;
import avatar.db.DbManager;
import avatar.lucky.DialLucky;
import avatar.model.Gift;
import avatar.network.Message;
import avatar.network.Session;
import avatar.model.User;
import avatar.service.Service;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.Setter;
import org.apache.log4j.Logger;

public class MapService extends Service {

    private static final Logger logger = Logger.getLogger(MapService.class);

    @Setter
    private Zone zone;

    public MapService(Session cl) {
        super(cl);
    }

    public void leavePark(int userID) {
        try {
            Message ms = new Message(Cmd.PLAYER_LEAVE_PARK);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("leavePark()", ex);
        }
    }

    public void move(User us) {
        try {
            Message ms = new Message(Cmd.MOVE_PARK);
            DataOutputStream ds = ms.writer();
            ds.writeInt(us.getId());
            ds.writeShort(us.getX());
            ds.writeShort(us.getY());
            ds.writeByte(us.getDirect());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("move()", ex);
        }
    }

    public void chat(User user, String text) {
        try {
            Message ms = new Message(zone.getMap().getId() == 22 ? Cmd.CHAT_FARM : Cmd.CHAT_PARK);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("chat()", ex);
        }
    }

    public void dialLucky(User user, short degree, List<Gift> gifts) {
        try {
            Message ms = new Message(Cmd.DIAL_LUCKY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeShort(degree);
            ds.writeByte(gifts.size());
            for (Gift gift : gifts) {
                ds.writeByte(gift.getType());
                switch (gift.getType()) {
                    case DialLucky.ITEM:
                        ds.writeShort(gift.getId());
                        ds.writeByte(gift.getExpireDay());
                        break;

                    case DialLucky.XU:
                        ds.writeInt(gift.getXu());
                        break;

                    case DialLucky.XP:
                        ds.writeInt(gift.getXp());
                        break;

                    case DialLucky.LUONG:
                        ds.writeInt(gift.getLuong());
                        break;
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("dialLucky", ex);
        }
    }

    public void doAction(int userID, int idTo, short action) {
        try {
            Message ms = new Message(59);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeInt(idTo);
            ds.writeShort(action);
            if (action == -1) {
                ds.writeUTF("Có thằng nào vừa làm cái gì đó, thông báo admin biết nhen !");
            } else {
                ds.writeShort(10);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("doAction()", e);
        }
    }

    public void doAvatarFeel(int userID, byte idFeel) {
        try {
            Message ms = new Message(Cmd.AVATAR_FEEL);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(idFeel);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doAvatarFeel()", ex);
        }
    }

    public void addPlayer(User us) {
        try {
            Message ms = new Message(51);
            DataOutputStream ds = ms.writer();
            ds.writeInt(us.getId());
            ds.writeUTF(us.getUsername());
            ds.writeByte((byte) us.getWearing().size());
            for (Item item : us.getWearing()) {
                ds.writeShort(item.getId());
            }
            ds.writeShort(us.getX());
            ds.writeShort(us.getY());
            //star
            ds.writeByte(us.getStar());
            // pet hunger
            ds.writeByte(50);
            // clan
            String sql = "SELECT c.icon, c.description FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE cm.user_id = ? AND cm.accept = 1";
            try (Connection connection = DbManager.getInstance().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, us.getId());  // Giả sử us.getId() trả về ID của user hiện tại
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
            //hẹn hò
            if(us.getIdUsHenHo() !=0&&us.getLevelMarry() ==0){
                ds.writeShort(2);
            } else if (us.getLevelMarry() > 0 && us.getLevelMarry()<5) {
                ds.writeShort(1153);
            } else if (us.getLevelMarry() > 4 && us.getLevelMarry()<10) {
                ds.writeShort(1154);
            } else if (us.getLevelMarry() > 9 && us.getLevelMarry()<15) {
                ds.writeShort(1155);
            }else if (us.getLevelMarry() > 14 && us.getLevelMarry()<20) {
                ds.writeShort(1156);
            } else if (us.getLevelMarry() > 19 && us.getLevelMarry()<24) {
                ds.writeShort(1157);
            }else {
                ds.writeShort(1096);//VIP IMG
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("addPlayer()", ex);
        }
    }

    public void usingPart(int userID, short itemID) {
        try {
            Message ms = new Message(Cmd.USING_PART);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeShort(itemID);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("usingPart()", ex);
        }
    }

    public void sendMessage(Message ms) {
        List<User> players = zone.getPlayers();
        synchronized (players) {
            for (User us : players) {
                us.sendMessage(ms);
            }
        }
    }
}
