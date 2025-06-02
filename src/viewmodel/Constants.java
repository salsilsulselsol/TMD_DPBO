package viewmodel;

public class Constants {
    public static final int GAME_WIDTH = 800;  // Lebar layar game
    public static final int GAME_HEIGHT = 600; // Tinggi layar game
    
    // Player
    public static final int PLAYER_WIDTH = 80;  // Misal, nilai saat ini adalah 60
    public static final int PLAYER_HEIGHT = 80; // Misal, nilai saat ini adalah 60

    public static final int PLAYER_INITIAL_HEARTS = 3;
    public static final float PLAYER_START_X = (GAME_WIDTH - PLAYER_WIDTH) / 2f; 
    public static final float PLAYER_START_Y = (GAME_HEIGHT - PLAYER_HEIGHT) / 2f;

    // Jellyfish (Bola Keterampilan)
    public static final int JELLYFISH_WIDTH = 40;
    public static final int JELLYFISH_HEIGHT = 40;
    public static final int MAX_JELLYFISH_ON_SCREEN = 7; // Jumlah maksimal di layar

    // Jar (Keranjang)
    public static final int JAR_WIDTH = 80;
    public static final int JAR_HEIGHT = 100;
    // Posisi Jar disesuaikan agar terlihat baik
    public static final float JAR_X = Constants.GAME_WIDTH - JAR_WIDTH - 25; 
    public static final float JAR_Y = (Constants.GAME_HEIGHT - JAR_HEIGHT) / 2f - 30;

    // Game Timer
    public static final int INITIAL_GAME_TIME_SECONDS = 90;    // Durasi game
    public static final int TIME_BONUS_PER_CATCH_SECONDS = 4; // Bonus waktu per tangkapan

    // Struggle Mechanic
    public static final int STRUGGLE_BAR_MAX_VALUE = 100;     // Target bar
    public static final int STRUGGLE_TAP_VALUE = 10;          // Poin per tap space
    public static final int STRUGGLE_TIME_LIMIT_MS = 3800;    // Waktu untuk struggle
}