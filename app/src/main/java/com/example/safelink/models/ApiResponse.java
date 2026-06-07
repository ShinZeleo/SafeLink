package com.example.safelink.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() { return data; }

    public String getTitle() { return "VirusTotal Scan"; }
    public String getFavicon() { return ""; }
    public int getRiskScore() {
        if (data != null && data.getAttributes() != null && data.getAttributes().getLastAnalysisStats() != null) {
            int malicious = data.getAttributes().getLastAnalysisStats().getMalicious();
            if (malicious > 0) return 100;
        }
        return 0;
    }

    /**
     * Feature 3: Return detailed per-engine results as a flat list for display in RecyclerView.
     */
    public List<EngineResultItem> getEngineResults() {
        List<EngineResultItem> items = new ArrayList<>();
        if (data == null || data.getAttributes() == null) return items;
        Map<String, EngineResult> results = data.getAttributes().getLastAnalysisResults();
        if (results == null) return items;

        for (Map.Entry<String, EngineResult> entry : results.entrySet()) {
            EngineResultItem item = new EngineResultItem();
            item.engineName = entry.getKey();
            item.category = entry.getValue().getCategory();
            item.result = entry.getValue().getResult();
            items.add(item);
        }

        // Sort: Malicious first, then Suspicious, then others
        items.sort((a, b) -> {
            int scoreA = categoryScore(a.category);
            int scoreB = categoryScore(b.category);
            return Integer.compare(scoreB, scoreA); // descending
        });

        return items;
    }

    private int categoryScore(String cat) {
        if (cat == null) return 0;
        switch (cat.toLowerCase()) {
            case "malicious": return 3;
            case "suspicious": return 2;
            case "harmless": return 1;
            default: return 0;
        }
    }

    // ---------- Inner Classes ----------

    public static class Data {
        @SerializedName("attributes")
        private Attributes attributes;

        public Attributes getAttributes() { return attributes; }
    }

    public static class Attributes {
        @SerializedName("last_analysis_stats")
        private LastAnalysisStats lastAnalysisStats;

        @SerializedName("last_analysis_results")
        private Map<String, EngineResult> lastAnalysisResults;

        public LastAnalysisStats getLastAnalysisStats() { return lastAnalysisStats; }
        public Map<String, EngineResult> getLastAnalysisResults() { return lastAnalysisResults; }
    }

    public static class LastAnalysisStats {
        @SerializedName("harmless")
        private Integer harmless;

        @SerializedName("malicious")
        private Integer malicious;

        @SerializedName("suspicious")
        private Integer suspicious;

        @SerializedName("undetected")
        private Integer undetected;

        public int getHarmless() { return harmless != null ? harmless : 0; }
        public int getMalicious() { return malicious != null ? malicious : 0; }
        public int getSuspicious() { return suspicious != null ? suspicious : 0; }
        public int getUndetected() { return undetected != null ? undetected : 0; }
        public int getTotal() { return getHarmless() + getMalicious() + getSuspicious() + getUndetected(); }
    }

    public static class EngineResult {
        @SerializedName("category")
        private String category;

        @SerializedName("result")
        private String result;

        @SerializedName("engine_name")
        private String engineName;

        public String getCategory() { return category; }
        public String getResult() { return result; }
        public String getEngineName() { return engineName; }
    }

    /** Flat item for RecyclerView display */
    public static class EngineResultItem {
        public String engineName;
        public String category;
        public String result;
    }
}
