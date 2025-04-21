package avatar.Farm;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FarmCooking {
    private final int id;
    private String name;
    private int require1;
    private int quantity1;
    private int require2;
    private int quantity2;
    private int time;
    private int product;
    
    public FarmCooking(int id) {
        this.id = id;
    }
    
    @Builder
    public FarmCooking(int id,String name, int require1, int quantity1, int require2, int quantity2,int time, int product) {
        this.id = id;
        this.name = name;
        this.require1 = require1;
        this.quantity1 = quantity1;
        this.require2 = require2;
        this.quantity2 = quantity2;
        this.time = time;
        this.product = product;
    }    
}
