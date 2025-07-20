package com.example.testapp.util

import java.net.URLDecoder

fun safeDecode(text: String): String = try {
    URLDecoder.decode(text, "UTF-8")
} catch (e: IllegalArgumentException) {
    text
}