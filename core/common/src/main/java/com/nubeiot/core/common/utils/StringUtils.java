package com.nubeiot.core.common.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;

/**
 * Merge in @{@link Strings}
 */
@Deprecated
public class StringUtils {
    public static String[] removeQuotes(String[] tokens) {
        String[] tokenWithoutQuote = new String[tokens.length];
        int i = 0;
        for (String item : tokens) {
            tokenWithoutQuote[i] = item.replaceAll("\"", "").trim();
            i++;
        }
        return tokenWithoutQuote;
    }

    public static boolean isNull(String field) {
        if (field == null)
            return true;
        else
            field = field.trim();

        return (field.equalsIgnoreCase("NULL") || field.equalsIgnoreCase("") || field.isEmpty());
    }

    public static boolean isNotNull(String field) {
        return !isNull(field);
    }

    public static int parseInt(String value, String defaultValue) {
        if (isNull(value)) {
            value = defaultValue;
        }
        if (!StringUtils.isNumeric(value)) {
            value = defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static long parseLong(String value, String defaultValue) {
        if (isNull(value)) {
            value = defaultValue;
        }
        if (!StringUtils.isNumeric(value)) {
            value = defaultValue;
        }
        return Long.parseLong(value);
    }

    public static double parseDouble(String value, String defaultValue) {
        if (isNull(value)) {
            value = defaultValue;
        }
        return Double.parseDouble(value);
    }

    public static float parseFloat(String value, String defaultValue) {
        if (isNull(value)) {
            value = defaultValue;
        }
        return Float.parseFloat(value);
    }

    public static String toUpperFirstChar(String text) {
        char[] c = text.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }

    public static String toLowerFirstChar(String text) {
        char[] c = text.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    public static String left(String text, int length) {
        if (StringUtils.isNull(text) || text.length() <= length) {
            return (text);
        } else {
            return text.substring(0, length);
        }
    }

    public static String right(String text, int length) {
        if (StringUtils.isNull(text) || text.length() <= length) {
            return (text);
        } else {
            return text.substring(text.length() - length, text.length());
        }
    }

    public static String mid(String text, int start, int end) {
        return text.substring(start, end);
    }

    public static String mid(String text, int start) {
        return text.substring(start, text.length() - start);
    }

    /**
     * http://stackoverflow.com/questions/1102891/how-to-check-a-string-is-a-numeric-type-in-java
     */
    public static boolean isNumeric(String str) {
        return StringUtils.isNotNull(str) && (str.trim().matches("[-\\+]?\\d+(\\.\\d+)?") || str.trim().matches("[-\\+]?+(\\.\\d+)?") || /*match a -ve number that ends with (.) */ str.trim().matches("[-\\+]?\\d+(\\.)?"));  //match a number with optional '-' and decimal.
        //or match a number with optional '-' and start with '.'
    }

    //followed by decimal
    public static String titleCase(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                chars[i] = Character.toUpperCase(chars[i]);
            } else if ((i + 1) < chars.length && chars[i] == ' ') {
                chars[i + 1] = Character.toUpperCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public static boolean numericallyEqual(String a, String b) {
        boolean status = false;
        double minValue = 0.005; // minimum difference between expected & actual value, which can be ignored
        if (StringUtils.isNotNull(a) && StringUtils.isNotNull(b) && StringUtils.isNumeric(a) && StringUtils.isNumeric(b)) {
            double value1 = Double.parseDouble(a);
            double value2 = Double.parseDouble(b);

            if (value1 >= value2) {
                if (value1 == value2 || (value1 - value2) < minValue)
                    status = true;
            } else {
                if ((value2 - value1) < minValue) {
                    status = true;
                }
            }
        }
        return status;
    }

    public static String repeat(String str, int count) {
        String ret = "";
        for (int i = 0; i < count; i++)
            ret += str;
        return ret;
    }

    public static String getDoubleString(String value, String defaultValue) {
        if (isNull(value) || !isNumeric(value)) {
            return defaultValue;
        } else {
            return String.valueOf(Double.parseDouble(value));
        }
    }

    public static String getRoundedInt(String value, String defaultValue) {
        if (!isNumeric(value)) {
            value = defaultValue;
        }
        return String.valueOf(Math.round(parseDouble(value, defaultValue)));
    }

    public static String getDoubleWithTwoDecimalString(String value, String defaultValue) {
        if (isNull(value) || !isNumeric(value) || value.equals("-")) {
            return defaultValue;
        } else {
            DecimalFormat df = new DecimalFormat("0.00");
            return String.valueOf(df.format(Double.parseDouble(value)));
        }
    }

    public static String getDoubleWithOneDecimalString(String value, String defaultValue) {
        if (isNull(value) || !isNumeric(value)) {
            return defaultValue;
        } else {
            DecimalFormat df = new DecimalFormat("0.0");
            return String.valueOf(df.format(Double.parseDouble(value)));
        }
    }

    public static String[] getIds(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids.toArray(new String[_ids.size()]);
    }

    public static List<String> getIdsList(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }

    public static JsonArray getIdsJsonArray(List<JsonObject> jsonObjectList) {
        JsonArray _ids = new JsonArray();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }
}
