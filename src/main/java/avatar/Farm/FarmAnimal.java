package avatar.Farm;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FarmAnimal {
    private int species;
    private String name;
    private int pricexu;
    private int priceluong;
    private int harvesttime;
    private int numproduct;
    
    public FarmAnimal(int species) {
        this.species = species;
    }
    
    @Builder
    public FarmAnimal(int species, String name, int pricexu, int priceluong, int harvesttime, int numproduct) {
        this.species = species;
        this.name = name;
        this.pricexu = pricexu;
        this.priceluong = priceluong;
        this.harvesttime = harvesttime;
        this.numproduct = numproduct;
    }
    
}
