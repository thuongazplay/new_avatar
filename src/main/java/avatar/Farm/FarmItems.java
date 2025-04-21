package avatar.Farm;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FarmItems {
    private int id;
    private int idimg;
    private int type;
    private String action;
    private String description;
    private int pricexu;
    private int priceluong;
    private int isitem;
    
    public FarmItems(int id) {
        this.id = id;
    }
    
    @Builder
    public FarmItems(int id, int idimg, int type, String action, String description, int pricexu, int priceluong, int isitem) {
        this.id = id;
        this.idimg = idimg;
        this.type = type;
        this.action = action;
        this.description = description;
        this.pricexu = pricexu;
        this.priceluong = priceluong;
        this.isitem = isitem;       
    }
    
}
