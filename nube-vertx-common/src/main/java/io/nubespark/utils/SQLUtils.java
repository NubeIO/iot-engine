package io.nubespark.utils;


public class SQLUtils {
    public static boolean in(String field, String... str) {
        for (String value : str) {
            if (StringUtils.isNotNull(field) && StringUtils.isNotNull(value) && field.equals(value))
                return true;
        }
        return false;
    }

    public static boolean inEqualsIgnoreCase(String field, String... str) {
        for (String value : str) {
            if (value.equalsIgnoreCase(field))
                return true;
        }
        return false;
    }

    public static boolean containsIgnoreCase(String field, String... str) {
        for (String value : str) {
            if (StringUtils.isNotNull(field) && StringUtils.isNotNull(value) && field.toLowerCase().contains(value.toLowerCase()))
                return true;
        }
        return false;
    }

    public static String[] getFirstNotNullArray(String[]... array) {
        for (int j = 0; j < array.length; j++)
            for (int k = 0; k < array[j].length; k++)
                if (StringUtils.isNotNull(array[j][k]))
                    return array[j];
        return array[0];
    }

    public static String getMatchValueOrDefaultOne(String toMatch, String[] withMatch) {
        for (String value : withMatch) {
            if (toMatch.equals(value)) {
                return toMatch;
            }
        }
        return withMatch[0];
    }
}
