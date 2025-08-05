package com.example.testapp.util

import java.net.URLDecoder
import java.net.URLEncoder

fun safeDecode(text: String): String = try {
    // Pre-check for malformed URL patterns
    if (text.contains("%-") && !text.matches(Regex(".*%[0-9A-Fa-f]{2}.*"))) {
        // Contains malformed % patterns, return original text
        text
    } else {
        URLDecoder.decode(text, "UTF-8")
    }
} catch (e: IllegalArgumentException) {
    // Return original text if decoding fails
    text
} catch (e: Exception) {
    // Catch any other exception and return original text
    text
}

fun safeEncode(text: String): String = try {
    URLEncoder.encode(text, "UTF-8")
} catch (e: Exception) {
    // If encoding fails, manually escape only essential characters
    text.replace("%", "%25")
        .replace(" ", "%20")
        .replace("#", "%23")
        .replace("?", "%3F")
        .replace("/", "%2F")
}