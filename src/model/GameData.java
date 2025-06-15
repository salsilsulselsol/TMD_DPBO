package model;

// Kelas ini adalah Plain Old Java Object (POJO) atau Data Transfer Object (DTO).
// Tujuannya hanya untuk membungkus dan memindahkan data hasil permainan.
public class GameData {
    // Properti untuk menyimpan data hasil satu sesi permainan.
    private String username;
    private int skor;
    private int count;

    // Konstruktor default (kosong).
    public GameData() {}

    // Konstruktor untuk membuat objek dengan data yang sudah terisi.
    public GameData(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    // Kumpulan metode getter dan setter standar untuk mengakses dan mengubah properti.
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getSkor() { return skor; }
    public void setSkor(int skor) { this.skor = skor; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}