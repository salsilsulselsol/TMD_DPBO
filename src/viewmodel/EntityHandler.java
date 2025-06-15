package viewmodel;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import model.BigFish;
import model.DartFish;
import model.Fish;
import model.GameObject;
import model.Ghost;

// Kelas ini bertanggung jawab untuk mengelola semua entitas dinamis (ikan dan hantu) di dalam game.
public class EntityHandler {
    // Daftar semua entitas yang aktif di layar. Dibuat thread-safe dengan synchronizedList.
    private final List<GameObject> entities; 
    private Random random = new Random();
    
    // Variabel untuk mengatur interval waktu kemunculan entitas secara acak.
    private long lastSpawnTime = 0;
    private long spawnIntervalMinMs = 1200; 
    private long spawnIntervalVarMs = 2000; 
    private long currentSpawnInterval;
    
    // Batas jumlah maksimum entitas di layar untuk menjaga performa.
    private int maxEntitiesOnScreen = 8; 

    // Konstruktor, menginisialisasi daftar entitas dan interval spawn pertama.
    public EntityHandler() {
        this.entities = Collections.synchronizedList(new ArrayList<>());
        setNextSpawnInterval();
    }
    
    // Mengatur waktu acak untuk kemunculan entitas berikutnya.
    private void setNextSpawnInterval() {
        currentSpawnInterval = spawnIntervalMinMs + random.nextInt((int)spawnIntervalVarMs + 1);
    }

    // Metode utama yang dipanggil di setiap frame dari game loop untuk mengupdate semua entitas.
    public void updateEntities() { 
        trySpawnNewEntity(); // Coba untuk memunculkan entitas baru.
        
        // Menggunakan 'synchronized' untuk menghindari error saat mengakses list dari thread yang berbeda.
        synchronized (entities) { 
            // Menggunakan Iterator agar aman saat menghapus entitas dari list selagi iterasi berjalan.
            Iterator<GameObject> iterator = entities.iterator();
            while (iterator.hasNext()) {
                GameObject entity = iterator.next();
                entity.update(); // Panggil metode update dari masing-masing entitas.

                // Cek apakah entitas sudah keluar dari batas layar.
                boolean remove = false;
                if (entity instanceof Fish) { 
                    if (((Fish) entity).isOutOfBounds()) {
                        remove = true;
                    }
                } else if (entity instanceof Ghost) {
                    if (((Ghost) entity).isOutOfBounds()) {
                        remove = true;
                    }
                }
                // Jika sudah keluar batas, hapus dari daftar.
                if (remove) {
                    iterator.remove();
                }
            }
        }
    }

    // Cek apakah sudah waktunya untuk memunculkan entitas baru.
    private void trySpawnNewEntity() {
        long currentTime = System.currentTimeMillis();
        
        // Spawn jika waktu interval sudah terlewati dan jumlah entitas di layar belum maksimal.
        if (currentTime - lastSpawnTime > currentSpawnInterval && entities.size() < maxEntitiesOnScreen) {
            spawnRandomEntity(); 
            lastSpawnTime = currentTime; // Catat waktu spawn terakhir.
            setNextSpawnInterval(); // Tentukan interval waktu untuk spawn berikutnya.
        }
    }

    // Logika untuk membuat entitas baru secara acak (jenis ikan atau hantu).
    private void spawnRandomEntity() {
        float spawnY;
        boolean movesLeftToRight = random.nextBoolean(); 
        
        float entityRenderWidthMaxPlaceholder = 60; 
        float spawnX = movesLeftToRight ? 0 - entityRenderWidthMaxPlaceholder : Constants.GAME_WIDTH + 20;

        String spriteSheetPath;
        GameObject newEntity = null;

        // Tentukan tipe entitas yang akan di-spawn dengan probabilitas tertentu.
        int type = random.nextInt(100); 

        if (type < 35) { // 35% Fish
            spriteSheetPath = "/assets/images/fish.png"; 
            spawnY = 32 + random.nextInt(Constants.GAME_HEIGHT - (32 * 2) - 60);
            newEntity = new Fish(spawnX, spawnY, spriteSheetPath, movesLeftToRight);
        } else if (type < 55) { // 20% BigFish
            spriteSheetPath = "/assets/images/fish-big.png"; 
            spawnY = 49 + random.nextInt(Constants.GAME_HEIGHT - (49 * 2) - 60);
            newEntity = new BigFish(spawnX, spawnY, spriteSheetPath, movesLeftToRight);
        } else if (type < 75) { // 20% DartFish
            spriteSheetPath = "/assets/images/fish-dart.png"; 
            spawnY = 20 + random.nextInt(Constants.GAME_HEIGHT - (20 * 2) - 60);
            newEntity = new DartFish(spawnX, spawnY, spriteSheetPath, movesLeftToRight);
        } else { // 25% Ghost
            spriteSheetPath = "/assets/images/ghost.png"; 
            spawnY = 66 + random.nextInt(Constants.GAME_HEIGHT - (66 * 2) - 60);
            newEntity = new Ghost(spawnX, spawnY, spriteSheetPath, movesLeftToRight);
        }
        
        // Tambahkan entitas baru ke dalam daftar jika berhasil dibuat.
        if (newEntity != null) {
            synchronized (entities) {
                entities.add(newEntity);
            }
        }
    }
    
    // Menghapus satu entitas spesifik dari daftar (misal: setelah ikan ditangkap).
    public void removeEntity(GameObject entity) {
        synchronized (entities) {
            entities.remove(entity);
        }
    }

    // Menggambar semua entitas yang ada di dalam daftar ke layar.
    public void renderEntities(Graphics g) { 
        synchronized (entities) { 
            for (GameObject entity : entities) { 
                entity.render(g);
            }
        }
    }

    // Mengembalikan salinan dari daftar entitas agar kelas lain bisa membacanya tanpa mengubah daftar asli.
    public List<GameObject> getEntities() { 
        synchronized (entities) {
            return new ArrayList<>(entities); 
        }
    }
    
    // Menghapus semua entitas dari daftar (digunakan saat memulai game baru).
    public void reset() { 
        synchronized (entities) {
            entities.clear();
        }
        lastSpawnTime = 0; // Reset timer spawn.
        setNextSpawnInterval();
    }
}