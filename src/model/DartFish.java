package model;

public class DartFish extends Fish {

    public DartFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        super(x, y, 39, 20, 15, spriteSheetPath, movesLeftToRight, 2.8f, 
              39, 20, 4, 100); 
    }
}