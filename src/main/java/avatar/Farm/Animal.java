package avatar.Farm;

public class Animal {
    private int id;             // ID động vật
    private long bornTime;      // Timestamp thời gian sinh
    private int growTime;       // Thời gian trưởng thành (giây)
    private int health;         // Máu (HP)
    private int harvestProgress; // Tiến độ thu hoạch (%)
    private int quantity;       // Sản lượng (trứng, sữa...)
    private boolean isAlive;    // Còn sống hay không
    private boolean isHungry;   // Đói hay không
    private boolean isSick;     // Bị bệnh hay không

    // Constructor
    public Animal(int id, long bornTime, int growTime, int health, int harvestProgress, int quantity, boolean isAlive, boolean isHungry, boolean isSick) {
        this.id = id;
        this.bornTime = bornTime;
        this.growTime = growTime;
        this.health = health;
        this.harvestProgress = harvestProgress;
        this.quantity = quantity;
        this.isAlive = isAlive;
        this.isHungry = isHungry;
        this.isSick = isSick;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBornTime() {
        return bornTime;
    }

    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    public int getGrowTime() {
        return growTime;
    }

    public void setGrowTime(int growTime) {
        this.growTime = growTime;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHarvestProgress() {
        return harvestProgress;
    }

    public void setHarvestProgress(int harvestProgress) {
        this.harvestProgress = harvestProgress;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isHungry() {
        return isHungry;
    }

    public void setHungry(boolean hungry) {
        isHungry = hungry;
    }

    public boolean isSick() {
        return isSick;
    }

    public void setSick(boolean sick) {
        isSick = sick;
    }

    // Trả về trạng thái trưởng thành
    public boolean isMature() {
        long currentTime = System.currentTimeMillis() / 1000; // Thời gian hiện tại (giây)
        return (currentTime >= (bornTime + growTime));
    }

    // Trả về loại động vật dựa trên ID
    public byte getType() {
        return (byte) id;
    }

    @Override
    public String toString() {
        return "Animal{" +
                "id=" + id +
                ", bornTime=" + bornTime +
                ", growTime=" + growTime +
                ", health=" + health +
                ", harvestProgress=" + harvestProgress +
                ", quantity=" + quantity +
                ", isAlive=" + isAlive +
                ", isHungry=" + isHungry +
                ", isSick=" + isSick +
                ", isMature=" + isMature() +
                '}';
    }
}
