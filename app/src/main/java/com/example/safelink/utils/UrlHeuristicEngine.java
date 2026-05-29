package com.example.safelink.utils;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

public class UrlHeuristicEngine {

    // IP address pattern
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // Common shorteners
    private static final String[] SHORTENERS = {
            "bit.ly", "t.co", "tinyurl.com", "is.gd", "goo.gl", "ow.ly", "buff.ly", "adf.ly"
    };
    
    // Trusted Domains
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
            "paypal.com", "shopee.co.id", "tokopedia.com", "github.com",
            "google.com", "youtube.com", "facebook.com", "instagram.com",
            "twitter.com", "bca.co.id", "mandiri.co.id", "bri.co.id"
    );

    public static boolean isSuspicious(String url) {
        if (url == null || url.isEmpty()) return false;

        String lowerUrl = url.toLowerCase();
        
        // 1. Excessive length
        if (url.length() > 100) return true;

        // Extract host
        String host = getHost(lowerUrl);
        if (host == null) return false;

        // 2. IP address
        if (IP_PATTERN.matcher(host).matches()) return true;

        // 3. Shortener
        for (String shortener : SHORTENERS) {
            if (host.equals(shortener) || host.endsWith("." + shortener)) {
                return true;
            }
        }

        // 4. Excessive numbers or symbols
        int digits = 0;
        int symbols = 0;
        for (char c : host.toCharArray()) {
            if (Character.isDigit(c)) digits++;
            else if (!Character.isLetterOrDigit(c) && c != '.' && c != '-') symbols++;
        }
        
        if (digits > 5 || symbols > 3) return true;

        // 5. Too many hyphens
        int hyphens = 0;
        for (char c : host.toCharArray()) {
            if (c == '-') hyphens++;
        }
        if (hyphens > 3) return true;

        return false;
    }
    
    public static boolean isTrustedDomain(String url, List<String> extraTrusted) {
        String host = getHost(url != null ? url.toLowerCase() : "");
        if (host == null) return false;
        if (host.startsWith("www.")) host = host.substring(4);
        
        List<String> allTrusted = new java.util.ArrayList<>(TRUSTED_DOMAINS);
        if (extraTrusted != null) allTrusted.addAll(extraTrusted);
        
        return allTrusted.contains(host);
    }
    
    public static String getTypoSquattedDomain(String url, List<String> extraTrusted) {
        String host = getHost(url != null ? url.toLowerCase() : "");
        if (host == null) return null;
        
        // Remove 'www.' for comparison
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        // Combine default and user trusted domains
        List<String> allTrusted = new java.util.ArrayList<>(TRUSTED_DOMAINS);
        if (extraTrusted != null) {
            allTrusted.addAll(extraTrusted);
        }

        // If it's exactly a trusted domain, it's not typosquatting
        if (allTrusted.contains(host)) return null;

        // Check Levenshtein distance against trusted domains
        for (String trusted : allTrusted) {
            int distance = calculateLevenshteinDistance(host, trusted);
            // If length is similar and distance is small (1 or 2 edits), flag as typo
            if (distance > 0 && distance <= 2) {
                // To avoid flagging very short domains incorrectly, add a length check
                if (trusted.length() > 5) {
                    return trusted;
                }
            }
        }
        
        return null;
    }

    public static String generateSmartRecommendation(String url, String typoDomain) {
        if (typoDomain != null) {
            return "Possible " + typoDomain + " impersonation detected. Avoid entering credentials.";
        }
        
        if (url == null || url.isEmpty()) return "Stay vigilant.";
        String lowerUrl = url.toLowerCase();
        String host = getHost(lowerUrl);
        if (host == null) return "Stay vigilant.";

        if (IP_PATTERN.matcher(host).matches()) {
            return "IP address URL detected. Proceed with extreme caution.";
        }

        for (String shortener : SHORTENERS) {
            if (host.equals(shortener) || host.endsWith("." + shortener)) {
                return "Avoid logging into accounts through shortened URLs.";
            }
        }

        if (isSuspicious(url)) {
            return "Domain structure looks suspicious. Do not download files.";
        }

        return "Looks generally safe, but always verify the site's identity.";
    }

    private static String getHost(String url) {
        try {
            String host = url;
            if (host.startsWith("http://")) host = host.substring(7);
            else if (host.startsWith("https://")) host = host.substring(8);
            
            int slashIdx = host.indexOf('/');
            if (slashIdx != -1) host = host.substring(0, slashIdx);
            
            int portIdx = host.indexOf(':');
            if (portIdx != -1) host = host.substring(0, portIdx);
            
            return host;
        } catch (Exception e) {
            return null;
        }
    }
    
    // Standard Levenshtein distance algorithm
    private static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
