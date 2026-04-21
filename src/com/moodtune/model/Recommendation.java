package com.moodtune.model;

public class Recommendation {
    private int id;
    private String mood;
    private String category;
    private String title;
    private String imageUrl;
    private String link;
    private double rating;
    private String platform;

    public Recommendation(int id, String mood, String category, String title,
                          String imageUrl, String link, double rating, String platform) {
        this.id = id;
        this.mood = mood;
        this.category = category;
        this.title = title;
        this.imageUrl = imageUrl;
        this.link = link;
        this.rating = rating;
        this.platform = platform;
    }

    // Getters
    public int getId()           { return id; }
    public String getMood()      { return mood; }
    public String getCategory()  { return category; }
    public String getTitle()     { return title; }
    public String getImageUrl()  { return imageUrl; }
    public String getLink()      { return link; }
    public double getRating()    { return rating; }
    public String getPlatform()  { return platform; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%.1f★) — %s", category.toUpperCase(), title, rating, platform);
    }
}
