package avatar.race;

public class PetInfo {
    private byte petId;
    private int amount;
    private int rate;
    private long timestamp;

    public PetInfo(byte petId, int amount, int rate) {
        this.petId = petId;
        this.amount = amount;
        this.rate = rate;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public byte getPetId() { return petId; }
    public int getAmount() { return amount; }
    public int getRate() { return rate; }
    public long getTimestamp() { return timestamp; }
    
    // Add setters if needed
    public void setAmount(int amount) { this.amount = amount; }
} 