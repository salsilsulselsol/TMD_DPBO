package model;

public class GameData {
    private String username;
    private int skor;
    private int count;

    public GameData() {}

    public GameData(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getSkor() { return skor; }
    public void setSkor(int skor) { this.skor = skor; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}