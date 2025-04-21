package avatar.Farm;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class farmwarehouseitem {
    private int id;
    private String name;
    private int price;
    
    public farmwarehouseitem(int id) {
        this.id = id;
    }
    
    @Builder
    public farmwarehouseitem(int iditem,String name,int price) {
        this.id = iditem;
        this.name = name;
        this.price = price;
    }
}
