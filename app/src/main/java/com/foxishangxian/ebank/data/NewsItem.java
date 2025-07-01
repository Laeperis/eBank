package com.foxishangxian.ebank.data;

public class NewsItem {
    private String title;
    private String description;
    private String url;
    private String publishedAt;
    private String source;

    public NewsItem(String title, String description, String url, String publishedAt, String source) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.publishedAt = publishedAt;
        this.source = source;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getPublishedAt() { return publishedAt; }
    public String getSource() { return source; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setUrl(String url) { this.url = url; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public void setSource(String source) { this.source = source; }
} 