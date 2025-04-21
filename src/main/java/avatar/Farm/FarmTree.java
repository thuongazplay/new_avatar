package avatar.Farm;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FarmTree {
    private int id;
    private String name;
    private int harvesttime;
    private int pricexu;
    private int priceluong;
    private int productid;
    private int numproduct;
    private int level;
    
    public FarmTree(int id) {
        this.id = id;
    }
    
    @Builder
    public FarmTree(int id, String name, int harvesttime, int pricexu, int priceluong, int productid, int numproduct, int level) {
        this.id = id;
        this.name = name;
        this.harvesttime = harvesttime;
        this.pricexu = pricexu;
        this.priceluong = priceluong;
        this.productid = productid;
        this.numproduct = numproduct;
        this.level = level;
    }
    
}
