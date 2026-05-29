package com.example.safelink.utils;

public class SecurityScoreEngine {
    
    public static int calculateScore(String status, String riskLevel, boolean isHttps, String typoDomain, boolean isTrusted) {
        if (isTrusted) {
            return isHttps ? 100 : 90; // Trusted domains are safe, slightly penalize if not HTTPS (though highly unlikely for trusted domains)
        }

        int score = 100;
        
        if (!isHttps) {
            score -= 20;
        }
        
        if (typoDomain != null && !typoDomain.isEmpty()) {
            score -= 40;
        }
        
        if ("Dangerous".equalsIgnoreCase(status)) {
            score = Math.min(score, 20); // Max 20 if dangerous
            if (riskLevel != null && riskLevel.toLowerCase().contains("malware")) {
                score = 5;
            }
        } else if ("Suspicious".equalsIgnoreCase(status)) {
            score -= 30;
        }
        
        return Math.max(0, Math.min(100, score));
    }
}
