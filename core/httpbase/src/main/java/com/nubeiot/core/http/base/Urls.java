package com.nubeiot.core.http.base;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * URL Utilities.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1738">[RFC 1738] Uniform Resource Locators</a>
 * @see <a href="https://tools.ietf.org/html/rfc1738#section-5">BNF URL schema</a>
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-2">Character encoding</a>
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Urls {

    public static final String HOST_PATTERN = "(www\\.)?(([\\w-]+\\.)+[\\w]{2,63}|[\\w-\\.]+)/?";
    /**
     * Authority syntax.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.2">Authority syntax</a>
     */
    public static final String AUTHORITY_PATTERN = "(www\\.)?(([\\w-]+\\.)+[\\w]{2,63}|[\\w-\\.]+)(:[1-9]\\d{1,4})?/?";
    /**
     * Path syntax.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">Path syntax</a>
     */
    public static final String PATH_PATTERN
        = "(/([\\w\\$\\-\\.\\+\\!\\*\\'\\(\\)\\,\\;\\:\\@\\&\\=]|(\\%[a-f0-9]{2}))*)*";
    /**
     * URL syntax. In this application case, any {@code query} or {@code fragment} will be not accepted.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3">URL syntax</a>
     */
    public static final String URL_PATTERN = HttpScheme.SCHEME_REGEX + AUTHORITY_PATTERN + PATH_PATTERN;

    /**
     * URL Path separator character.
     */
    public static final String PATH_SEP_CHAR = "/";
    /**
     * URL Query separator character.
     */
    public static final String QUERY_SEP_CHAR = "?";
    /**
     * URL Fragment separator character.
     */
    public static final String FRAGMENT_SEP_CHAR = "#";

    /**
     * Prefix capture param
     */
    public static final String CAPTURE_PARAM = "/:";

    private static final Map<String, String> ENCODING_RULES;

    static {
        final Map<String, String> rules = new HashMap<>();
        rules.put("*", "%2A");
        rules.put("+", "%20");
        rules.put("%7E", "~");
        ENCODING_RULES = Collections.unmodifiableMap(rules);
    }

    /**
     * Optimize URL with validation and normalize forward splash ({@code /}).
     *
     * @param base First segment, typically is {@code URL}
     * @param path Second segment, typically is {@code Path}
     * @return String combination of base and path that conforms to {@code URL} syntax
     */
    public static String optimizeURL(String base, String path) {
        if (Strings.isBlank(base) && Strings.isBlank(path)) {
            throw new IllegalArgumentException("Base URl and path are blank");
        }
        if (validateURL(path)) {
            return path;
        }
        if (validateURL(base)) {
            if (Strings.isBlank(path)) {
                return base;
            }
            String normalizePath = normalize(path);
            if (!validatePath(normalizePath)) {
                throw new InvalidUrlException("Invalid path: " + path);
            }
            return normalize(base + normalizePath);
        }
        throw new InvalidUrlException("Invalid url - Base: " + base + " - Path: " + path);
    }

    /**
     * Validate URL.
     *
     * @param url String to serialize
     * @return {@code True} if given input is valid URL syntax, otherwise {@code False}
     * @see #URL_PATTERN
     */
    public static boolean validateHost(String url) {
        return validate(url, HOST_PATTERN);
    }

    /**
     * Validate URL.
     *
     * @param url String to serialize
     * @return {@code True} if given input is valid URL syntax, otherwise {@code False}
     * @see #URL_PATTERN
     */
    public static boolean validateURL(String url) {
        return validate(url, URL_PATTERN);
    }

    /**
     * Validate URL Path.
     *
     * @param path String to serialize
     * @return {@code True} if given input is valid URL syntax, otherwise {@code False}
     * @see #PATH_PATTERN
     */
    public static boolean validatePath(String path) {
        return validate(path, PATH_PATTERN);
    }

    public static String combinePath(@NonNull String... path) {
        return normalize(
            PATH_SEP_CHAR + Arrays.stream(path).filter(Strings::isNotBlank).collect(Collectors.joining(PATH_SEP_CHAR)));
    }

    public static String normalize(@NonNull String url) {
        return url.replaceAll("/+", PATH_SEP_CHAR).replaceAll("^(https?:)/", "$1//");
    }

    private static boolean validate(String s, String pattern) {
        return Strings.isNotBlank(s) && Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(s).matches();
    }

    /**
     * Build a complete URL.
     *
     * @param scheme HTTP Scheme
     * @param host   Host
     * @param port   Port. Port &lt; 1 will be skipped
     * @return URL based on given input. Nullable if given host is blank
     */
    public static String buildURL(HttpScheme scheme, String host, int port) {
        if (Strings.isBlank(host)) {
            return null;
        }
        return scheme + "://" + normalize(host) + (port < 1 ? "" : ":" + port);
    }

    /**
     * Build a complete URL.
     *
     * @param url   Base URL with path
     * @param query Encode query
     * @return URL based on given input
     * @throws IllegalArgumentException if {@code url} is null or empty
     */
    public static String buildURL(String url, String query) {
        return buildURL(url, query, null);
    }

    /**
     * Build a complete URL.
     *
     * @param url      Base URL with path
     * @param query    Encode query
     * @param fragment Encode fragment
     * @return URL based on given input
     * @throws IllegalArgumentException if {@code url} is null or empty
     */
    public static String buildURL(String url, String query, String fragment) {
        return normalize(Strings.requireNotBlank(url) + buildQuery(query) + buildFragment(fragment));
    }

    private static String buildFragment(String fragment) {
        return Strings.isBlank(fragment) ? "" : FRAGMENT_SEP_CHAR + fragment;
    }

    private static String buildQuery(String query) {
        return Strings.isBlank(query) ? "" : QUERY_SEP_CHAR + query;
    }

    /**
     * Encode plain text in {@code UTF-8} encoding and follow standardization format.
     *
     * @param plain text to encode
     * @return Encoded value
     * @see <a href="https://tools.ietf.org/html/rfc5849#section-3.6">Percent encoding</a>
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-2.2">Reserved Characters</a>
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-2.3">Unreserved Characters</a>
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-2.4">When to Encode or Decode</a>
     */
    public static String encode(String plain) {
        Objects.requireNonNull(plain, "Cannot encode null object");
        String encoded;
        try {
            encoded = URLEncoder.encode(plain, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException uee) {
            throw new NubeException("Charset not found while encoding string: " + StandardCharsets.UTF_8, uee);
        }
        for (Map.Entry<String, String> rule : ENCODING_RULES.entrySet()) {
            encoded = applyRule(encoded, rule.getKey(), rule.getValue());
        }
        return encoded;
    }

    /**
     * Decode encoded text for human readable.
     *
     * @param encoded Encoded value to decode
     * @return Plain text
     */
    public static String decode(String encoded) {
        Objects.requireNonNull(encoded, "Cannot decode null object");
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException uee) {
            throw new NubeException("Charset not found while decoding string: " + StandardCharsets.UTF_8, uee);
        }
    }

    /**
     * Construct a capture path that is plain text
     * <p>
     * For example: {@code /catalogue/products/:product_type/:product_id}
     *
     * @param path   Path
     * @param params list of param name
     * @return capture path
     * @see #capturePatternPath(String, String...)
     */
    public static String capturePath(@NonNull String path, String... params) {
        String p = normalize(PATH_SEP_CHAR + path);
        String pns = Arrays.stream(params)
                           .filter(Strings::isNotBlank)
                           .map(Urls::toCapture)
                           .collect(Collectors.joining());
        return Strings.isNotBlank(pns) ? normalize(p + pns) : p;
    }

    /**
     * Construct a capture path that is pattern
     * <p>
     * For example: {@code /points/:id/type/:type_id}
     *
     * @param path   Pattern path. E.g: /points/{0}/type/{1}
     * @param params list of param name
     * @return capture path
     */
    public static String capturePatternPath(@NonNull String path, String... params) {
        String p = normalize(PATH_SEP_CHAR + path);
        String[] pns = Arrays.stream(params).filter(Strings::isNotBlank).map(Urls::toCapture).toArray(String[]::new);
        return normalize(Strings.format(p, pns));
    }

    private static String applyRule(String encoded, String toReplace, String replacement) {
        return encoded.replaceAll(Pattern.quote(toReplace), replacement);
    }

    public static String toPathWithLC(@NonNull String text) {
        return Strings.transform(text, false, "-").replaceAll("_", "-");
    }

    public static String toCapture(String param) {
        final String s = Strings.requireNotBlank(param);
        if (s.startsWith(CAPTURE_PARAM)) {
            return s;
        }
        if (s.startsWith(PATH_SEP_CHAR)) {
            throw new InvalidUrlException("Invalid param path because of starting with /");
        }
        return CAPTURE_PARAM + s;
    }

}
