package com.example.safelink.models;

public class HistoryModel {

    private int id;
    private String url;
    private String status;
    private String riskLevel;
    private String recommendation;
    private String scannedAt;

    public HistoryModel() {
    }

    public HistoryModel(int id, String url, String status,
                        String riskLevel, String recommendation, String scannedAt) {
        this.id = id;
        this.url = url;
        this.status = status;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
        this.scannedAt = scannedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }
}
