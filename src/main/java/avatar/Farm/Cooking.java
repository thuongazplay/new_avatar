package avatar.Farm;

public class Cooking {
    private int id; // id mon an
    private long time; // thoi gian hoan thanh   
    
    // Contructor
    public Cooking(int id, long time) {
    this.id = id;
    this.time = time;
    }
    
    // Getter & Setter
    public int getId() {
        return id;
    }
    
    public void setId(int Id) {
        this.id = id;
    }
    
    public long getTime() {
        return time;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    @Override
    public String toString() {
        return "Cooking{" +
                "id=" + id +
                ", time=" + time +
                '}';
    }
}


