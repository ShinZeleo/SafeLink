package com.example.safelink.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Feature 4 (Upgraded): Enhanced offline heuristic engine.
 * Detects phishing keywords, suspicious TLDs, excessive subdomains,
 * IP-based URLs, URL shorteners, and typo-squatting.
 * Returns a numerical HeuristicScore (0-100) for display alongside the VirusTotal result.
 */
public class UrlHeuristicEngine {

    // IP address pattern
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // Common shorteners
    private static final String[] SHORTENERS = {
            "bit.ly", "t.co", "tinyurl.com", "is.gd", "goo.gl", "ow.ly",
            "buff.ly", "adf.ly", "short.link", "rb.gy", "cutt.ly"
    };

    // Trusted Domains
    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
            "paypal.com", "shopee.co.id", "tokopedia.com", "github.com",
            "google.com", "youtube.com", "facebook.com", "instagram.com",
            "twitter.com", "x.com", "bca.co.id", "mandiri.co.id", "bri.co.id",
            "microsoft.com", "apple.com", "amazon.com", "netflix.com",
            "linkedin.com", "whatsapp.com", "telegram.org", "tiktok.com",
            "spotify.com", "yahoo.com"
    );

    // Feature 4: Phishing-related keywords commonly found in fake URLs
    private static final String[] PHISHING_KEYWORDS = {
            "login", "signin", "verify", "secure", "update", "confirm",
            "account", "banking", "password", "credential", "wallet",
            "suspend", "unlock", "validate", "alert", "urgent",
            "limited", "offer", "free", "prize", "winner", "click",
            "support", "helpdesk", "recovery", "authenticate"
    };

    // Feature 4: Suspicious top-level domains often used for malicious sites
    private static final String[] SUSPICIOUS_TLDS = {
            ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".club",
            ".work", ".date", ".review", ".stream", ".download", ".zip",
            ".mov", ".gdn", ".bid", ".loan", ".win", ".racing", ".trade"
    };

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Quick boolean check – true if the URL looks suspicious.
     */
    public static boolean isSuspicious(String url) {
        return getHeuristicScore(url) >= 40;
    }

    /**
     * Feature 4: Returns a numerical heuristic risk score 0-100.
     * 0 = clean, 100 = very suspicious.
     * Scores are additive from individual checks.
     */
    public static int getHeuristicScore(String url) {
        if (url == null || url.isEmpty()) return 0;

        int score = 0;
        String lowerUrl = url.toLowerCase();
        String host = getHost(lowerUrl);
        if (host == null) return 0;

        // 1. Excessive URL length (> 100 chars = +20)
        if (url.length() > 75) score += 10;
        if (url.length() > 100) score += 10;

        // 2. IP address direct access (+40)
        if (IP_PATTERN.matcher(host).matches()) score += 40;

        // 3. URL shortener (+20)
        for (String shortener : SHORTENERS) {
            if (host.equals(shortener) || host.endsWith("." + shortener)) {
                score += 20;
                break;
            }
        }

        // 4. Feature 4: Phishing keyword in full URL (+15 each, max 45)
        int keywordHits = 0;
        for (String keyword : PHISHING_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                keywordHits++;
                score += 15;
                if (keywordHits >= 3) break; // cap at 45
            }
        }

        // 5. Feature 4: Suspicious TLD (+30)
        for (String tld : SUSPICIOUS_TLDS) {
            if (host.endsWith(tld)) {
                score += 30;
                break;
            }
        }

        // 6. Feature 4: Too many subdomains (e.g. login.secure.update.domain.com = 3 subs → +25)
        String domainPart = host.startsWith("www.") ? host.substring(4) : host;
        long subdomainCount = domainPart.chars().filter(c -> c == '.').count();
        if (subdomainCount >= 3) score += 25;
        else if (subdomainCount == 2) score += 10;

        // 7. Too many hyphens in domain (+15)
        int hyphens = 0;
        for (char c : host.toCharArray()) {
            if (c == '-') hyphens++;
        }
        if (hyphens > 3) score += 15;
        else if (hyphens > 1) score += 5;

        // 8. Excessive digits in domain (+10)
        int digits = 0;
        for (char c : host.toCharArray()) {
            if (Character.isDigit(c)) digits++;
        }
        if (digits > 5) score += 10;

        // 9. HTTP (not HTTPS) penalty (+10)
        if (lowerUrl.startsWith("http://")) score += 10;

        // 10. IDN Homograph attack check (+80)
        if (isIdnHomograph(host)) score += 80;

        // 11. Typo-squatting detection (+80)
        if (getTypoSquattedDomain(url, null) != null) score += 80;

        return Math.min(score, 100);
    }

    /**
     * Returns a human-readable label for the heuristic score.
     */
    public static String getHeuristicLabel(int score) {
        if (score >= 70) return "Tinggi";
        if (score >= 40) return "Sedang";
        return "Rendah";
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

        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        List<String> allTrusted = new java.util.ArrayList<>(TRUSTED_DOMAINS);
        if (extraTrusted != null) {
            allTrusted.addAll(extraTrusted);
        }

        if (allTrusted.contains(host)) return null;

        for (String trusted : allTrusted) {
            int distance = calculateLevenshteinDistance(host, trusted);
            if (distance > 0 && distance <= 2) {
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

        int score = getHeuristicScore(url);
        if (score >= 70) return "URL shows multiple high-risk patterns. Do not proceed.";
        if (score >= 40) return "Domain structure looks suspicious. Do not download files.";

        return "Looks generally safe, but always verify the site's identity.";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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

    /**
     * Feature: Detects IDN Homograph attacks.
     * Flags domains that use Punycode (xn--) or contain non-ASCII characters
     * which might be used to spoof legitimate domains.
     */
    private static boolean isIdnHomograph(String host) {
        if (host == null || host.isEmpty()) return false;
        
        // Check for Punycode prefix
        if (host.contains("xn--")) return true;
        
        // Check for non-ASCII characters (e.g., Cyrillic 'а', Greek)
        for (char c : host.toCharArray()) {
            if (c > 127) {
                return true;
            }
        }
        return false;
    }

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
