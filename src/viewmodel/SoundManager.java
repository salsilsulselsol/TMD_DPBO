package viewmodel;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public class SoundManager {
    private SoundManager() {} // Kelas utilitas, tidak perlu diinstansiasi

    public static Clip playSound(String relativePathInsideClasspath, boolean loop) {
        try {
            // Path harus absolut dari root classpath, biasanya diawali "/"
            // Contoh: "/assets/sounds/menu_music.wav"
            String correctedPath = relativePathInsideClasspath.startsWith("/") ? relativePathInsideClasspath : "/" + relativePathInsideClasspath;
            URL soundURL = SoundManager.class.getResource(correctedPath); 
            
            if (soundURL == null) {
                System.err.println("Sound file not found: " + correctedPath + 
                                   ". Pastikan path benar dan file ada di build path (misal, di dalam folder src).");
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            if (!loop) {
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        Clip c = (Clip) event.getSource();
                        if (c.isOpen()) { // Cek jika masih open sebelum close
                           c.close(); 
                        }
                    }
                });
            }

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound " + relativePathInsideClasspath + ": " + e.getMessage());
            // e.printStackTrace(); // Bisa diaktifkan untuk debugging lebih detail
            return null;
        }
    }

    public static void stopSound(Clip clip) {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            if (clip.isOpen()) { // Hanya tutup jika masih terbuka
                clip.close();
            }
        }
    }
}