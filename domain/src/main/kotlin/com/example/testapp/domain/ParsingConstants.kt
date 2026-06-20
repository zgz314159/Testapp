package com.example.testapp.domain

object ParsingConstants {
    // Keep keys for non-UI code; actual localized strings live in res/values/strings.xml
    const val PARSING_KEY = "parsing"
    const val PARSE_FAILED_PREFIX_KEY = "parse_failed"
    const val PARSE_FAILED_PREFIX_COLON_KEY = "parse_failed_colon"
    const val PARSE_FAILED_EMPTY_KEY = "parse_failed_empty"
    const val PARSE_FAILED_NETWORK_KEY = "parse_failed_network"
    const val PARSE_FAILED_CANNOT_CONNECT_KEY = "parse_failed_cannot_connect"
    const val PARSE_FAILED_ALL_RETRIES_KEY = "parse_failed_all_retries"
    // NOTE: Removed UI text literals — use string resources in UI layer.
    // These constants are keys only; UI should resolve to localized strings.
    const val PARSING = PARSING_KEY
    const val PARSE_FAILED_PREFIX = PARSE_FAILED_PREFIX_KEY
    const val PARSE_FAILED_PREFIX_COLON = PARSE_FAILED_PREFIX_COLON_KEY
    const val PARSE_FAILED_EMPTY = PARSE_FAILED_EMPTY_KEY
    const val PARSE_FAILED_NETWORK = PARSE_FAILED_NETWORK_KEY
    const val PARSE_FAILED_CANNOT_CONNECT = PARSE_FAILED_CANNOT_CONNECT_KEY
    const val PARSE_FAILED_ALL_RETRIES = PARSE_FAILED_ALL_RETRIES_KEY
}
