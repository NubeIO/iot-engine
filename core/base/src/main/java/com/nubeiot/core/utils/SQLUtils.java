package com.nubeiot.core.utils;

public class SQLUtils {

    public static boolean in(String with, boolean equalsIgnoreCase, String... values) {
        if (Strings.isBlank(with)) {
            return false;
        }

        for (String value : values) {
            if (Strings.isNotBlank(value) && equalsIgnoreCase ? with.equalsIgnoreCase(value) : with.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean in(String with, String... values) {
        return in(with, false, values);
    }

    public static String getFirstNotNull(String... strings) {
        for (final String string : strings) {
            if (Strings.isNotBlank(string)) {
                return string;
            }
        }
        return strings[0];
    }

    public static Object getFirstNotNull(Object... objects) {
        for (final Object object : objects) {
            if (object != null) {
                return object;
            }
        }
        return objects[0];
    }

    public static String getMatchValueOrFirstOne(String with, String[] withMatch) {
        for (String value : withMatch) {
            if (with.equals(value)) {
                return with;
            }
        }
        return withMatch.length > 0 ? withMatch[0] : null;
    }

    public static String getMatchValue(String with, String[] withMatch) {
        for (String value : withMatch) {
            if (with.equals(value)) {
                return with;
            }
        }
        return null;
    }

}

