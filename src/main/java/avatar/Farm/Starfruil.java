package avatar.Farm;

import java.time.Duration;
import java.time.LocalDateTime;

public class Starfruil {
    private int level;                // Cấp độ của cây khế
    private int quantity;             // Sản lượng (số lượng trái khế)
    private int timeToHarvest;        // Thời gian để cây ra trái (phút)
    private LocalDateTime timeToUpgrade;        // Thời gian nâng cấp cây (phút)
    private LocalDateTime plantedTime; // Thời gian cây được trồng hoặc nâng cấp xong

    // Constructor
    public Starfruil(int level, int quantity, int timeToHarvest, LocalDateTime timeToUpgrade, LocalDateTime plantedTime) {
        this.level = level;
        this.quantity = quantity;
        this.timeToHarvest = timeToHarvest;
        this.timeToUpgrade = timeToUpgrade;
        this.plantedTime = plantedTime;
    }

    // Getters và Setters
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTimeToHarvest() {
        return timeToHarvest;
    }

    public void setTimeToHarvest(int timeToHarvest) {
        this.timeToHarvest = timeToHarvest;
    }

    public LocalDateTime getTimeToUpgrade() {
        return timeToUpgrade;
    }

    public void setTimeToUpgrade(LocalDateTime timeToUpgrade) {
        this.timeToUpgrade = timeToUpgrade;
    }

    public LocalDateTime getPlantedTime() {
        return plantedTime;
    }

    public void setPlantedTime(LocalDateTime plantedTime) {
        this.plantedTime = plantedTime;
    }

    // Tính thời gian còn lại để ra trái (phút)
    public long getMinutesToHarvest() {
        if (this.plantedTime == null) {
            throw new IllegalStateException("Planted time is not set");
        }
        return Math.max(0, timeToHarvest - Duration.between(this.plantedTime, LocalDateTime.now()).toMinutes());
    }

    // Kiểm tra nếu cây đã sẵn sàng thu hoạch
    public boolean isHarvestable() {
        return getMinutesToHarvest() <= 0;
    }

    @Override
    public String toString() {
        return "Starfruil{" +
                "level=" + level +
                ", quantity=" + quantity +
                ", timeToHarvest=" + timeToHarvest +
                ", timeToUpgrade=" + timeToUpgrade +
                ", plantedTime=" + plantedTime +
                '}';
    }
}
