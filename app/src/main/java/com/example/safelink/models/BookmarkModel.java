package com.example.safelink.models;

public class BookmarkModel {
    private int id;
    private String url;
    private String title;
    private String category;
    private String notes;
    private String faviconUrl;
    private String status;
    private String scannedAt;
    private String apiResponseJson;

    public BookmarkModel() {}

    public BookmarkModel(int id, String url, String title, String category, String notes, String faviconUrl, String status, String scannedAt, String apiResponseJson) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.category = category;
        this.notes = notes;
        this.faviconUrl = faviconUrl;
        this.status = status;
        this.scannedAt = scannedAt;
        this.apiResponseJson = apiResponseJson;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getFaviconUrl() { return faviconUrl; }
    public void setFaviconUrl(String faviconUrl) { this.faviconUrl = faviconUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getScannedAt() { return scannedAt; }
    public void setScannedAt(String scannedAt) { this.scannedAt = scannedAt; }
    public String getApiResponseJson() { return apiResponseJson; }
    public void setApiResponseJson(String apiResponseJson) { this.apiResponseJson = apiResponseJson; }
}
