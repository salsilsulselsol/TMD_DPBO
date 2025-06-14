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

public class EntityHandler {
    private final List<GameObject> entities; 
    private Random random = new Random();
    
    private long lastSpawnTime = 0;
    private long spawnIntervalMinMs = 1200; 
    private long spawnIntervalVarMs = 2000; 
    private long currentSpawnInterval;
    
    private int maxEntitiesOnScreen = 8; 

    public EntityHandler() {
        this.entities = Collections.synchronizedList(new ArrayList<>());
        setNextSpawnInterval();
    }
    
    private void setNextSpawnInterval() {
        currentSpawnInterval = spawnIntervalMinMs + random.nextInt((int)spawnIntervalVarMs + 1);
    }

    public void updateEntities() { 
        trySpawnNewEntity(); 
        
        synchronized (entities) { 
            Iterator<GameObject> iterator = entities.iterator();
            while (iterator.hasNext()) {
                GameObject entity = iterator.next();
                entity.update(); 

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
                if (remove) {
                    iterator.remove();
                }
            }
        }
    }

    private void trySpawnNewEntity() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastSpawnTime > currentSpawnInterval && entities.size() < maxEntitiesOnScreen) {
            spawnRandomEntity(); 
            lastSpawnTime = currentTime;
            setNextSpawnInterval();
        }
    }

    private void spawnRandomEntity() {
        float spawnY;
        boolean movesLeftToRight = random.nextBoolean(); 
        
        float entityRenderWidthMaxPlaceholder = 60; 
        float spawnX = movesLeftToRight ? 0 - entityRenderWidthMaxPlaceholder : Constants.GAME_WIDTH + 20;

        String spriteSheetPath;
        GameObject newEntity = null;

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
        
        if (newEntity != null) {
            synchronized (entities) {
                entities.add(newEntity);
            }
        }
    }
    
    public void removeEntity(GameObject entity) {
        synchronized (entities) {
            entities.remove(entity);
        }
    }

    public void renderEntities(Graphics g) { 
        synchronized (entities) { 
            for (GameObject entity : entities) { 
                entity.render(g);
            }
        }
    }

    public List<GameObject> getEntities() { 
        synchronized (entities) {
            return new ArrayList<>(entities); 
        }
    }
    
    public void reset() { 
        synchronized (entities) {
            entities.clear();
        }
        lastSpawnTime = 0;
        setNextSpawnInterval();
    }
}