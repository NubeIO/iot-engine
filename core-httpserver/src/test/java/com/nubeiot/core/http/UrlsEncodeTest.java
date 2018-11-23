package com.nubeiot.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UrlsEncodeTest {

    @Test(expected = NullPointerException.class)
    public void test_encode_null() {
        Urls.encode(null);
    }

    @Test(expected = NullPointerException.class)
    public void test_decode_null() {
        Urls.decode(null);
    }

    @Test
    public void test_plus_sign() {
        System.out.println("Plus sign: \u002B");
        assertEquals("%2B", Urls.encode("+"));
        assertEquals("+", Urls.decode("%2B"));
    }

    @Test
    public void test_space_sign() {
        System.out.println("Space sign: '\u0020'");
        assertEquals("%20", Urls.encode(" "));
        assertEquals(" ", Urls.decode("%20"));
    }

    @Test
    public void test_percent_sign() {
        System.out.println("Percent sign: \u0025");
        assertEquals("%25", Urls.encode("%"));
        assertEquals("%", Urls.decode("%25"));
    }

    @Test
    public void test_ampersand_sign() {
        System.out.println("Ampersand sign: \u0026");
        assertEquals("%26", Urls.encode("&"));
        assertEquals("&", Urls.decode("%26"));
    }

    @Test
    public void test_pound_sign() {
        System.out.println("Pound sign: \u00A3");
        assertEquals("%C2%A3", Urls.encode("\u00A3"));
        assertEquals("\u00A3", Urls.decode("%C2%A3"));
    }

    @Test
    public void test_euro_sign() {
        System.out.println("Euro sign: \u20AC");
        assertEquals("%E2%82%AC", Urls.encode("\u20AC"));
        assertEquals("\u20AC", Urls.decode("%E2%82%AC"));
    }

    @Test
    public void test_tilde_sign() {
        System.out.println("Tilde sign: \u007E");
        assertEquals("~", Urls.encode("~"));
        assertEquals("~", Urls.decode("~"));
    }

    @Test
    public void test_hyphen_sign() {
        System.out.println("Hyphen sign: \u002D");
        assertEquals("-", Urls.encode("-"));
        assertEquals("-", Urls.decode("-"));
    }

    @Test
    public void test_underscore_sign() {
        System.out.println("Underscore sign: \u005F");
        assertEquals("_", Urls.encode("_"));
        assertEquals("_", Urls.decode("_"));
    }

    @Test
    public void test_dot_sign() {
        System.out.println("Dot sign: \u002E");
        assertEquals(".", Urls.encode("."));
        assertEquals(".", Urls.decode("."));
    }

    @Test
    public void test_equal_sign() {
        assertEquals("%3D", Urls.encode("="));
        assertEquals("=", Urls.decode("%3D"));
    }

    @Test
    public void test_decode_FormURL_plus_to_space() {
        assertEquals("this is", Urls.decode("this+is"));
    }

    @Test
    public void test_decode_FormURL_equal_to_space() {
        assertEquals("this=that", Urls.decode("this%3Dthat"));
    }

    @Test
    public void test_reservedCharacters_noEncode() {
        final String plain = "abcdefghijklmnopqrstuxyzwABCDEFGHIJKLMNOPQRSTUXYZW1234567890-._~";
        assertEquals(plain, Urls.encode(plain));
        assertEquals(plain, Urls.decode(plain));
    }

}
