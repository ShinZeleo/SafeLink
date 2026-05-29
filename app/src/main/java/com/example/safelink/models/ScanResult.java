package com.example.safelink.models;

import androidx.annotation.NonNull;

public class ScanResult {

    public static final String STATUS_SAFE = "SAFE";
    public static final String STATUS_SUSPICIOUS = "SUSPICIOUS";
    public static final String STATUS_DANGEROUS = "DANGEROUS";

    private String url;
    private String status;
    private String riskLevel;
    private String recommendation;
    private String title;
    private String favicon;
    private int riskScore;
    private String scannedAt;

    public ScanResult() {
    }

    /**
     * Build ScanResult from raw ApiResponse.
     */
    @NonNull
    public static ScanResult fromApiResponse(@NonNull ApiResponse response, String originalUrl) {
        ScanResult result = new ScanResult();
        result.url = originalUrl;
        result.title = response.getTitle();
        result.favicon = response.getFavicon();
        result.riskScore = response.getRiskScore();

        int malicious = 0;
        int suspicious = 0;

        if (response.getData() != null && response.getData().getAttributes() != null && response.getData().getAttributes().getLastAnalysisStats() != null) {
            ApiResponse.LastAnalysisStats stats = response.getData().getAttributes().getLastAnalysisStats();
            malicious = stats.getMalicious();
            suspicious = stats.getSuspicious();
        }

        // Response mapping according to VirusTotal rules
        // DANGEROUS: Jika malicious >= threshold (1)
        // SUSPICIOUS: Jika suspicious > 0
        // SAFE: Jika harmless dominan
        if (malicious >= 1) {
            result.status = STATUS_DANGEROUS;
            result.riskLevel = "Tinggi";
            result.recommendation = "URL ini berpotensi phishing atau malware. Jangan buka link ini!";
        } else if (suspicious > 0) {
            result.status = STATUS_SUSPICIOUS;
            result.riskLevel = "Sedang";
            result.recommendation = "URL ini mencurigakan. Berhati-hati sebelum memasukkan data pribadi.";
        } else {
            result.status = STATUS_SAFE;
            result.riskLevel = "Rendah";
            result.recommendation = "URL ini aman untuk dibuka. Tetap waspada terhadap konten website.";
        }

        return result;
    }

    // --- Getters & Setters ---

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }
}
