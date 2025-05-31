package viewmodel;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator; // Untuk synchronizedList
import java.util.List;
import java.util.Random;
import model.Jellyfish;

public class JellyfishHandler {
    private final List<Jellyfish> jellyfishes;
    private Random random = new Random();
    
    private long lastSpawnTimeTop = 0;
    private long spawnIntervalTopMs_Min = 2000; 
    private long spawnIntervalTopMs_Var = 2500; 
    private long currentSpawnIntervalTop;

    private long lastSpawnTimeBottom = 0;
    private long spawnIntervalBottomMs_Min = 2300; // Sedikit beda interval
    private long spawnIntervalBottomMs_Var = 2700;
    private long currentSpawnIntervalBottom;

    public JellyfishHandler() {
        this.jellyfishes = Collections.synchronizedList(new ArrayList<>()); // List yang aman untuk thread
        setNextSpawnIntervals();
    }
    
    private void setNextSpawnIntervals() {
        currentSpawnIntervalTop = spawnIntervalTopMs_Min + random.nextInt((int)spawnIntervalTopMs_Var + 1);
        currentSpawnIntervalBottom = spawnIntervalBottomMs_Min + random.nextInt((int)spawnIntervalBottomMs_Var + 1);
    }

    public void updateJellyfish() {
        trySpawnNewJellyfish(); // Coba spawn ubur-ubur baru
        
        // Iterasi dan update ubur-ubur yang ada (aman untuk thread)
        synchronized (jellyfishes) { 
            Iterator<Jellyfish> iterator = jellyfishes.iterator();
            while (iterator.hasNext()) {
                Jellyfish jf = iterator.next();
                jf.update();
                if (jf.isOutOfBounds()) { // Hapus jika keluar layar
                    iterator.remove();
                }
            }
        }
    }

    private void trySpawnNewJellyfish() {
        long currentTime = System.currentTimeMillis();
        // PDF: "Munculnya bola dan nilainya dapat dibuat random dari kanan ke kiri untuk bagian atas, dan dari kiri ke kanan untuk bagian bawah." [cite: 39, 85]
        
        synchronized (jellyfishes) { // Sinkronisasi saat menambah ke list
            // Spawn untuk bagian atas (bergerak dari kanan ke kiri)
            if (currentTime - lastSpawnTimeTop > currentSpawnIntervalTop && jellyfishes.size() < Constants.MAX_JELLYFISH_ON_SCREEN) {
                float spawnY = 10 + random.nextInt(Constants.GAME_HEIGHT / 2 - Constants.JELLYFISH_HEIGHT - 20); // Area atas
                int score = 5 + random.nextInt(26); // Skor 5-30
                String imagePath = random.nextBoolean() ? "/assets/images/jellyfish_blue.png" : "/assets/images/jellyfish_pink.png"; // PASTIKAN ASET ADA

                Jellyfish newJfTop = new Jellyfish(Constants.GAME_WIDTH, spawnY, // Muncul dari kanan
                                            Constants.JELLYFISH_WIDTH, Constants.JELLYFISH_HEIGHT,
                                            score, imagePath, false); // false = kanan ke kiri
                jellyfishes.add(newJfTop);
                lastSpawnTimeTop = currentTime;
                currentSpawnIntervalTop = spawnIntervalTopMs_Min + random.nextInt((int)spawnIntervalTopMs_Var + 1);
            }

            // Spawn untuk bagian bawah (bergerak dari kiri ke kanan)
            if (currentTime - lastSpawnTimeBottom > currentSpawnIntervalBottom && jellyfishes.size() < Constants.MAX_JELLYFISH_ON_SCREEN) {
                float spawnY = Constants.GAME_HEIGHT / 2f + 10 + random.nextInt(Constants.GAME_HEIGHT / 2 - Constants.JELLYFISH_HEIGHT - 20); // Area bawah
                int score = 5 + random.nextInt(26);
                String imagePath = random.nextBoolean() ? "/assets/images/jellyfish_blue.png" : "/assets/images/jellyfish_pink.png"; // PASTIKAN ASET ADA

                Jellyfish newJfBottom = new Jellyfish(0 - Constants.JELLYFISH_WIDTH, spawnY, // Muncul dari kiri
                                                Constants.JELLYFISH_WIDTH, Constants.JELLYFISH_HEIGHT,
                                                score, imagePath, true); // true = kiri ke kanan
                jellyfishes.add(newJfBottom);
                lastSpawnTimeBottom = currentTime;
                currentSpawnIntervalBottom = spawnIntervalBottomMs_Min + random.nextInt((int)spawnIntervalBottomMs_Var + 1);
            }
        }
    }
    
    public void removeJellyfish(Jellyfish jf) {
        synchronized (jellyfishes) {
            jellyfishes.remove(jf);
        }
    }

    public void renderJellyfish(Graphics g) {
        synchronized (jellyfishes) { 
            // Iterasi menggunakan salinan untuk menghindari ConcurrentModificationException jika ada modifikasi dari thread lain
            // Namun, jika semua akses sudah disinkronisasi, iterasi langsung mungkin aman.
            // List<Jellyfish> toRender = new ArrayList<>(jellyfishes); // Salinan untuk render
            // for (Jellyfish jf : toRender) {
            //     jf.render(g);
            // }
            // Atau iterasi langsung jika sudah yakin aman:
            for (Jellyfish jf : jellyfishes) {
                jf.render(g);
            }
        }
    }

    public List<Jellyfish> getJellyfishes() { 
        synchronized (jellyfishes) {
            return new ArrayList<>(jellyfishes); // Kembalikan salinan agar list internal aman
        }
    }
    
    public void reset() { // Dipanggil saat game dimulai/di-restart
        synchronized (jellyfishes) {
            jellyfishes.clear();
        }
        lastSpawnTimeTop = 0;
        lastSpawnTimeBottom = 0;
        setNextSpawnIntervals();
    }
}