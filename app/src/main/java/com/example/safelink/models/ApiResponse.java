package com.example.safelink.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("data")
    private Data data;

    public Data getData() { return data; }

    // Dummy methods to avoid breaking existing UI that expects them
    // Even though VT doesn't provide these in the same way, we return defaults.
    public String getTitle() { return "VirusTotal Scan"; }
    public String getFavicon() { return ""; }
    public int getRiskScore() {
        if (data != null && data.getAttributes() != null && data.getAttributes().getLastAnalysisStats() != null) {
            int malicious = data.getAttributes().getLastAnalysisStats().getMalicious();
            if (malicious > 0) return 100;
        }
        return 0;
    }

    public static class Data {
        @SerializedName("attributes")
        private Attributes attributes;

        public Attributes getAttributes() { return attributes; }
    }

    public static class Attributes {
        @SerializedName("last_analysis_stats")
        private LastAnalysisStats lastAnalysisStats;

        public LastAnalysisStats getLastAnalysisStats() { return lastAnalysisStats; }
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
    }
}
