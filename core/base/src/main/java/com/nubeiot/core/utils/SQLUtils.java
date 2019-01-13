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

}
