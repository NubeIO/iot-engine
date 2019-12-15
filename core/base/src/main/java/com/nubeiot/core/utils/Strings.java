package com.nubeiot.core.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Strings Utilities.
 *
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Strings {

    private static final Logger logger = LoggerFactory.getLogger(Strings.class);

    /**
     * To String.
     *
     * @param object the object cast to string.
     * @return {@code blank} if null, else {@link Object#toString()} with {@code trim}
     */
    public static String toString(Object object) {
        return object == null ? "" : object.toString().trim();
    }

    /**
     * Construct from given value in multiple times.
     *
     * @param value Value to duplicate
     * @param times Times to duplicate
     * @return Append value
     */
    public static String duplicate(String value, int times) {
        Objects.requireNonNull(value);
        if (times <= 1) {
            throw new IllegalArgumentException("Duplicate time must be greater than 1");
        }
        return IntStream.range(0, times)
                        .collect(StringBuilder::new, (s, i) -> s.append(value), (s, i) -> s.append(value))
                        .toString();
    }

    /**
     * Check given text is blank or not.
     *
     * @param text the text to check for blank
     * @return {@code True} if blank, else otherwise
     */
    public static boolean isBlank(String text) {
        return text == null || "".equals(text.trim());
    }

    /**
     * Check given text is not blank or not. The reversion of {@link #isBlank(String)}
     *
     * @param text the text to check for blank
     * @return {@code True} if not blank, else otherwise
     */
    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    /**
     * Checks that the specified string reference is not {@code blank}. This method is designed primarily for doing
     * parameter validation in methods and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = Strings.requireNotBlank(bar);
     * }
     * </pre></blockquote>
     *
     * @param text the text to check for blank
     * @return Trimmed {@code text} if not {@code blank}
     * @throws IllegalArgumentException if {@code obj} is {@code blank}
     */
    public static String requireNotBlank(String text) {
        return requireNotBlank(text, "Given input cannot be empty");
    }

    /**
     * Checks that the specified string reference is not {@code blank}. This method is designed primarily for doing
     * parameter validation in methods and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = Strings.requireNotBlank(bar, "String cannot blank");
     * }
     * </pre></blockquote>
     *
     * @param text    the text to check for blank
     * @param message the error message will be included in exception
     * @return Trimmed {@code text} if not {@code blank}
     * @throws IllegalArgumentException if {@code obj} is {@code blank}
     */
    public static String requireNotBlank(String text, String message) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
        return text.trim();
    }

    /**
     * Checks that the specified string reference is not {@code blank}. This method is designed primarily for doing
     * parameter validation in methods and constructors, as demonstrated below:
     * <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = Strings.requireNotBlank(bar, "String cannot blank");
     * }
     * </pre></blockquote>
     *
     * @param text    the text to check for blank
     * @param message the error message will be included in exception
     * @return Trimmed {@code text} if not {@code blank}
     * @throws IllegalArgumentException if {@code obj} is {@code blank}
     */
    public static String requireNotBlank(Object text, String message) {
        if (Objects.isNull(text) || isBlank(text.toString())) {
            throw new IllegalArgumentException(message);
        }
        return text.toString().trim();
    }

    /**
     * Checks that the specified string reference is not {@code blank} then remove multiple space characters to one
     * space.
     *
     * @param text Given input
     * @return Optimization text
     * @throws IllegalArgumentException if {@code text} is {@code blank}
     */
    public static String optimizeMultipleSpace(String text) {
        String t = requireNotBlank(text);
        return t.replaceAll("\\s+", "");
    }

    /**
     * Checks that the specified string reference is not {@code blank} then remove all space characters.
     *
     * @param text Given input
     * @return Optimization text
     * @throws IllegalArgumentException if {@code text} is {@code blank}
     */
    public static String optimizeNoSpace(String text) {
        String t = requireNotBlank(text);
        return t.replaceAll("\\s+", "");
    }

    /**
     * Checks that the specified string reference is not {@code blank} and its length greater than given input.
     *
     * @param text      Given input
     * @param minLength Min length
     * @return this text if conforms condition
     * @throws IllegalArgumentException if {@code text} or {@code optimized text} is {@code blank}
     */
    public static String requiredMinLength(String text, int minLength) {
        String t = requireNotBlank(text);
        if (t.length() < minLength) {
            throw new IllegalArgumentException("Text " + text + " length must be greater than " + minLength);
        }
        return t;
    }

    public static String toSnakeCaseLC(@NonNull String text) {
        return toSnakeCase(text, false);
    }

    public static String toSnakeCaseUC(@NonNull String text) {
        return toSnakeCase(text, true);
    }

    public static String toSnakeCase(@NonNull String text, boolean upper) {
        return transform(text, upper, "_");
    }

    public static String transform(@NonNull String text, boolean upper, String separate) {
        if (upper && text.equals(text.toUpperCase())) {
            return text;
        }
        if (!upper && text.equals(text.toLowerCase())) {
            return text;
        }
        String t = text.replaceAll("([A-Z])", separate + "$1").replaceAll("^" + separate, "");
        return upper ? t.toUpperCase() : t.toLowerCase();
    }

    /**
     * Convert {@code string} to {@code int}.
     *
     * @param text     Text to convert
     * @param fallback Default value to fallback
     * @return Int value that corresponding to given text
     */
    public static int convertToInt(String text, int fallback) {
        if (Strings.isBlank(text)) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            logger.debug(ex, "Cannot parse {0} to int", text);
            return fallback;
        }
    }

    public static String convertToString(InputStream inputStream) {
        try {
            return Objects.isNull(inputStream)
                   ? null
                   : FileUtils.convertToByteArray(inputStream).toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            logger.trace("Impossible", ex);
            return null;
        }
    }

    public static String format(String msgPattern, Object... params) {
        String[] args = Arrays.stream(params).map(String::valueOf).toArray(String[]::new);
        return MessageFormat.format(msgPattern, args);
    }

    /**
     * Checks given string is blank then fallback to given value
     *
     * @param value    String
     * @param fallback Fallback value, must not blank
     * @return given value if not blank, otherwise {@code fallback}
     */
    public static String fallback(String value, String fallback) {
        return Strings.isBlank(value) ? requireNotBlank(fallback) : value;
    }

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

    public static String kvMsg(@NonNull Object key, @NonNull Object value) {
        return key + "=" + value;
    }

    public static String kvMsg(@NonNull JsonObject json) {
        return json.stream()
                   .filter(entry -> Objects.nonNull(entry.getValue()))
                   .map(kvMsg())
                   .collect(Collectors.joining(" and "));
    }

    @NonNull
    public static Function<Entry, String> kvMsg() {
        return entry -> kvMsg(entry.getKey(), entry.getValue());
    }

}
