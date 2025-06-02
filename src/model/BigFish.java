package model;

public class BigFish extends Fish { 

    public BigFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        super(x, y, 54, 49, 25, spriteSheetPath, movesLeftToRight, 1.2f,
              54, 49, 4, 180); 
    }
}