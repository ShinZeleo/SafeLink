package com.example.safelink;

import com.example.safelink.utils.UrlHeuristicEngine;
import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityLogicTest {

    @Test
    public void testSuspiciousUrlDetection() {
        // Test IP based URL
        assertTrue(UrlHeuristicEngine.isSuspicious("http://192.168.1.1/login"));
        
        // Test Shortened URL
        assertTrue(UrlHeuristicEngine.isSuspicious("https://bit.ly/secure-login"));
        
        // Test Normal URL
        assertFalse(UrlHeuristicEngine.isSuspicious("https://google.com"));
        
        // Test Typo-squatting Pattern (many symbols)
        assertTrue(UrlHeuristicEngine.isSuspicious("https://secure-login-update-account-verify.com-sc-update.net"));
    }

    @Test
    public void testTypoSquattingLogic() {
        // Test domain that looks like google.com
        String result = UrlHeuristicEngine.getTypoSquattedDomain("https://g00gle.com", null);
        assertEquals("google.com", result);
        
        // Test legitimate domain
        String normal = UrlHeuristicEngine.getTypoSquattedDomain("https://google.com", null);
        assertNull(normal);
    }

    @Test
    public void testIdnHomographDetection() {
        // Punycode representation of homograph domain
        assertTrue(UrlHeuristicEngine.isSuspicious("https://xn--pypal-4ve.com")); // Punycode
        
        // Non-ASCII Cyrillic 'а' character domain
        assertTrue(UrlHeuristicEngine.isSuspicious("https://pаypal.com")); // contains Cyrillic 'a' (non-ASCII)
        
        // Plain ASCII domain should not trigger homograph alert
        assertFalse(UrlHeuristicEngine.isSuspicious("https://paypal.com"));
    }
}
