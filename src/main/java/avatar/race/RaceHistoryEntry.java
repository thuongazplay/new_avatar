package avatar.race;

public class RaceHistoryEntry {
    private short petId;
    private long raceTime;
    
    public RaceHistoryEntry(short petId, long raceTime) {
        this.petId = petId;
        this.raceTime = raceTime;
    }
    
    public short getPetId() {
        return petId;
    }
    
    public long getRaceTime() {
        return raceTime;
    }
} 