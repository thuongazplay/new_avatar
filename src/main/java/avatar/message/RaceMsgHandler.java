package avatar.message;

import java.io.IOException;

import avatar.constants.Cmd;
import avatar.network.Message;
import avatar.network.Session;
import avatar.service.RaceService;
import java.io.DataOutputStream;

public class RaceMsgHandler extends MessageHandler {

    private RaceService service;

    public RaceMsgHandler(Session client) {
        super(client);
        this.service = new RaceService(client);
    }

    @Override 
    public void onMessage(Message msg) {
        if (msg == null || this.client.user == null) {
            return;
        }
        
        try {
            switch (msg.getCommand()) {
                case 1: // Join race
                    service.handleJoinRace(msg);
                    break;
                case 2: // Get pet info
                    service.getPetInfo(msg.reader().readByte());
                    break;
                case 5: // Handle betting
                    service.handleBetting(msg);
                    break;
                case 9: // Handle chat
                    service.handleChat(msg);
                    break;
                case 8:
                    service.sendRaceHistory();
                    break;

                case Cmd.GET_IMG_ICON: {
                    if (this.client.user != null) {
                        this.client.doGetImgIcon(msg);
                            break;
                        }
                        break;
                    }                    
                    
                default:
                    System.out.println("RaceMsgHandler: " + msg.getCommand());
                    super.onMessage(msg);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}