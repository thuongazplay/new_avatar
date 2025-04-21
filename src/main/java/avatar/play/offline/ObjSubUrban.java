package avatar.play.offline;

import avatar.item.Item;
import avatar.model.Npc;
import java.util.ArrayList;

public class ObjSubUrban extends AbsMapOffline {
    
    public ObjSubUrban(int id) {
        super(id);
    }
    
    @Override
    public void init() {
        Npc doom = Npc.builder().id(1214)
                .name("dr doom")
                .x((short) 130)
                .y((short) 180)
                .wearing(new ArrayList<>()).build();
        doom.addItemToWearing(new Item(0));
        doom.addItemToWearing(new Item(4));
        doom.addItemToWearing(new Item(61));
        doom.addItemToWearing(new Item(538));
        doom.addItemToWearing(new Item(540));
        doom.addItemToWearing(new Item(485));
        doom.addItemToWearing(new Item(486));
        doom.addItemToWearing(new Item(4280));
        doom.addItemToWearing(new Item(6810));
        doom.addItemToWearing(new Item(6837));
        doom.addChat("Có những bí mật cần được bật mí");
        addNpc(doom);
    }
}
