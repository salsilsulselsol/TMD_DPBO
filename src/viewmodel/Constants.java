package viewmodel;

// Kelas ini berisi semua nilai konstan yang digunakan untuk pengaturan dan keseimbangan game.
public class Constants {

    // ==== DIMENSI GAME ====
    public static final int GAME_WIDTH = 800; // Lebar layar permainan dalam piksel.
    public static final int GAME_HEIGHT = 600; // Tinggi layar permainan dalam piksel.

    // ==== PENGATURAN PEMAIN ====
    public static final int PLAYER_WIDTH = 100;    // Lebar gambar pemain saat dirender.
    public static final int PLAYER_HEIGHT = 100;   // Tinggi gambar pemain saat dirender.
    public static final int PLAYER_INITIAL_HEARTS = 3; // Jumlah nyawa awal pemain.
    // Posisi awal pemain di sumbu X (dihitung agar di tengah).
    public static final float PLAYER_START_X = (GAME_WIDTH - PLAYER_WIDTH) / 2f;
    // Posisi awal pemain di sumbu Y (dihitung agar di tengah).
    public static final float PLAYER_START_Y = (GAME_HEIGHT - PLAYER_HEIGHT) / 2f;

    // ==== PENGATURAN JAR (KERANJANG) ====
    public static final int JAR_WIDTH = 96;  // Lebar gambar keranjang.
    public static final int JAR_HEIGHT = 120; // Tinggi gambar keranjang.
    // Posisi keranjang di sumbu X (sisi kanan layar).
    public static final float JAR_X = GAME_WIDTH - JAR_WIDTH - 30;
    // Posisi keranjang di sumbu Y (tengah).
    public static final float JAR_Y = (GAME_HEIGHT - JAR_HEIGHT) / 2f;

    // ==== PENGATURAN WAKTU PERMAINAN ====
    public static final int INITIAL_GAME_TIME_SECONDS = 90; // Waktu awal permainan dalam detik.
    public static final int TIME_BONUS_PER_CATCH_SECONDS = 4; // Bonus waktu (detik) yang didapat setiap berhasil menangkap ikan.

    // ==== MEKANISME STRUGGLE SAAT MENANGKAP IKAN ====
    public static final int STRUGGLE_BAR_MAX_VALUE = 100;     // Nilai progress bar yang harus dicapai untuk memenangkan struggle.
    public static final int STRUGGLE_TAP_VALUE = 10;          // Poin yang didapat per tekanan tombol saat struggle.
    public static final int STRUGGLE_TIME_LIMIT_MS = 3000;    // Batas waktu untuk menyelesaikan struggle dalam milidetik.
}