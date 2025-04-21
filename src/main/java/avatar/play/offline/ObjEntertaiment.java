package avatar.play.offline;

import avatar.item.Item;
import avatar.model.Npc;
import java.util.ArrayList;

public class ObjEntertaiment extends AbsMapOffline {
    
    public ObjEntertaiment(int id) {
        super(id);
    }
    
    @Override
    public void init() {
        Npc thitruong = Npc.builder().id(1215)
                .name("thi truong")
                .x((short) 250)
                .y((short) 160)
                .wearing(new ArrayList<>()).build();
        thitruong.addItemToWearing(new Item(0));
        thitruong.addItemToWearing(new Item(4));
        thitruong.addItemToWearing(new Item(40));
        thitruong.addItemToWearing(new Item(41));
        thitruong.addItemToWearing(new Item(42));
        thitruong.addItemToWearing(new Item(540));
        thitruong.addItemToWearing(new Item(40));
        thitruong.addItemToWearing(new Item(4688));
        thitruong.addChat("Chào mừng bạn đến với thành phố này");
        thitruong.addChat("Thành phố này trông thật đẹp đúng không nào");
        thitruong.addChat("Bạn có muốn làm nhiệm vụ không ?");
        addNpc(thitruong);
    }
    
}
