package viewmodel;

/**
 * Kelas ini berisi semua nilai konstan yang digunakan di seluruh permainan.
 * Menggunakan kelas ini memudahkan untuk mengubah pengaturan dan keseimbangan game 
 * di satu tempat terpusat.
 */
public class Constants {

    // ==== DIMENSI GAME ====
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 600;

    // ==== PENGATURAN PEMAIN ====
    public static final int PLAYER_WIDTH = 80;    // Lebar render pemain di layar
    public static final int PLAYER_HEIGHT = 80;   // Tinggi render pemain di layar
    public static final int PLAYER_INITIAL_HEARTS = 3; // Jumlah nyawa awal pemain
    // Posisi awal pemain di tengah layar
    public static final float PLAYER_START_X = (GAME_WIDTH - PLAYER_WIDTH) / 2f;
    public static final float PLAYER_START_Y = (GAME_HEIGHT - PLAYER_HEIGHT) / 2f;

    // ==== PENGATURAN JAR (KERANJANG) ====
    public static final int JAR_WIDTH = 140;  // Lebar render keranjang
    public static final int JAR_HEIGHT = 168; // Tinggi render keranjang
    // Posisi keranjang di kanan-tengah layar
    public static final float JAR_X = GAME_WIDTH - JAR_WIDTH - 25;
    public static final float JAR_Y = (GAME_HEIGHT - JAR_HEIGHT) / 2f;

    // ==== PENGATURAN WAKTU PERMAINAN ====
    public static final int INITIAL_GAME_TIME_SECONDS = 90; // Waktu awal permainan (detik)
    public static final int TIME_BONUS_PER_CATCH_SECONDS = 4; // Bonus waktu per tangkapan (detik)

    // ==== MEKANISME STRUGGLE SAAT MENANGKAP IKAN ====
    public static final int STRUGGLE_BAR_MAX_VALUE = 100;     // Nilai bar yang harus dicapai untuk berhasil
    public static final int STRUGGLE_TAP_VALUE = 10;          // Poin yang didapat per tekanan spasi
    public static final int STRUGGLE_TIME_LIMIT_MS = 2500;    // Batas waktu struggle (milidetik), disesuaikan agar lebih menantang
}