package com.nubeiot.buildscript.jooq

class DB {
    static TYPES = [
        varchar  : Utils.toRegexIgnoreCase("N?VARCHAR"),
        text     : Utils.toRegexIgnoreCase("TEXT|CLOB"),
        timestamp: Utils.toRegexIgnoreCase("TIMESTAMP")
    ]
    static COL_REGEX = [
        json     : Utils.toRegexIgnoreCase(".+_JSON\$"),
        jsonArray: Utils.toRegexIgnoreCase(".+(_JSON_ARRAY|_ARRAY)\$"),
    ]
}

static def requireNotBlank(String text, String message) {
    if (text == null || "" == text.trim()) {
        throw new IllegalArgumentException(message)
    }
    return text.trim()
}

static def toSnakeCase(String text, boolean upper = true) {
    if (upper && text == text.toUpperCase()) {
        return text
    }
    if (!upper && text == text.toLowerCase()) {
        return text
    }
    def regex = upper ? "A-Z" : "a-z"
    def t = text.replaceAll(/([$regex])/, /_$1/).replaceAll(/^_/, '')
    return upper ? t.toUpperCase() : t.toLowerCase()
}

static def replaceJsonSuffix(String name) {
    return name.replaceAll("_JSON(_ARRAY)?|_ARRAY\$", "")
}

static def toRegexIgnoreCase(String name) {
    return "(?i:${name})"
}