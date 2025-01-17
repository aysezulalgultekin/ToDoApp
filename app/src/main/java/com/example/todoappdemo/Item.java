package com.example.todoappdemo;

public class Item {
    private final String name;
    private final int levelRequirement;
    private final int imageResId;

    public Item(String name, int levelRequirement, int imageResId) {
        this.name = name;
        this.levelRequirement = levelRequirement;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public int getImageResId() {
        return imageResId;
    }
}
