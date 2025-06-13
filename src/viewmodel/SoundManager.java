package viewmodel;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundManager {
    // Cache untuk menyimpan klip SFX yang sudah di-load
    private static Map<String, Clip> sfxCache = new HashMap<>();
    
    // Klip khusus untuk BGM agar bisa di-loop dan dihentikan
    private static Clip bgmClip;

    /**
     * Metode ini dipanggil sekali di awal program untuk memuat semua SFX ke memori.
     */
    public static void init() {
        System.out.println("Initializing SoundManager and pre-loading sounds...");
        // Daftarkan semua efek suara pendek di sini
        loadSound("/assets/sounds/hit-sound.wav", "hit");
        loadSound("/assets/sounds/catch-sound.wav", "catch");
        loadSound("/assets/sounds/fail-sound.wav", "fail");
    }

    private static void loadSound(String path, String name) {
        try {
            URL soundURL = SoundManager.class.getResource(path);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            sfxCache.put(name, clip); // Simpan klip ke dalam cache dengan nama kuncinya
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound " + path + ": " + e.getMessage());
        }
    }
    
    /**
     * Memainkan efek suara (SFX) yang sudah di-load berdasarkan namanya.
     * @param name Nama suara yang didaftarkan di metode init().
     */
    public static void playSound(String name) {
        Clip clip = sfxCache.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // Hentikan jika sedang berjalan
            }
            clip.setFramePosition(0); // Putar dari awal
            clip.start();
        }
    }

    /**
     * Memainkan musik latar (BGM) secara berulang (loop).
     * @param path Path menuju file musik.
     */
    public static void playBGM(String path) {
        stopBGM(); 
        try {
            URL soundURL = SoundManager.class.getResource(path);
            if (soundURL == null) {
                System.err.println("BGM file not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioIn);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing BGM " + path + ": " + e.getMessage());
        }
    }

    /**
     * Menghentikan musik latar (BGM) yang sedang berjalan.
     */
    public static void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}