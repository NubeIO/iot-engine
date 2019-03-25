package com.nubeiot.core.http.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UrlsTest {

    private static final String DEFAULT_URL = "http://localhost:8080/api";

    @Test(expected = IllegalArgumentException.class)
    public void test_optimizeUrl_Blank() {
        Urls.optimizeURL(null, null);
    }

    @Test(expected = InvalidUrlException.class)
    public void test_optimizeUrl_InvalidBaseFormat() {
        Urls.optimizeURL("htt://abc", null);
    }

    @Test(expected = InvalidUrlException.class)
    public void test_optimizeUrl_InvalidPathFormat_BaseIsBlank() {
        Urls.optimizeURL(null, "abc");
    }

    @Test
    public void test_optimizeUrl_NullPath() {
        assertEquals(DEFAULT_URL, Urls.optimizeURL(DEFAULT_URL, null));
    }

    @Test
    public void test_optimizeUrl_BlankPath() {
        assertEquals(DEFAULT_URL, Urls.optimizeURL(DEFAULT_URL, ""));
    }

    @Test
    public void test_optimizeUrl_FullPath() {
        assertEquals(DEFAULT_URL + "/auth", Urls.optimizeURL(DEFAULT_URL, "/auth"));
    }

    @Test(expected = InvalidUrlException.class)
    public void test_optimizeUrl_PathWithSpace() {
        Urls.optimizeURL(DEFAULT_URL, "auth/abc xyz");
    }

    @Test
    public void test_optimizeUrl_MessyPath1() {
        assertEquals(DEFAULT_URL + "/auth/t1", Urls.optimizeURL(DEFAULT_URL, "//auth///t1"));
    }

    @Test
    public void test_optimizeUrl_MessyPath2() {
        assertEquals(DEFAULT_URL + "/auth/t2/", Urls.optimizeURL(DEFAULT_URL, "//auth///t2///"));
    }

    @Test(expected = InvalidUrlException.class)
    public void test_optimizeUrl_PathWithParameter() {
        assertEquals(DEFAULT_URL + "/auth?getName=x&age=20", Urls.optimizeURL(DEFAULT_URL, "auth?getName=x&age=20"));
    }

    @Test(expected = InvalidUrlException.class)
    public void test_optimizeUrl_PathWithEncodeParameter() {
        assertEquals(DEFAULT_URL + "/auth?getName=x%2By&age=20",
                     Urls.optimizeURL(DEFAULT_URL, "auth?getName=x+y&age=20"));
    }

    @Test
    public void test_optimizeUrl_PathIsUrl() {
        assertEquals("https://localhost/api", Urls.optimizeURL(DEFAULT_URL, "https://localhost/api"));
    }

    @Test
    public void test_validUrl_LocalWithoutPort() {
        assertTrue(Urls.validateURL("https://localhost"));
    }

    @Test
    public void test_validUrl_LocalWithPort() {
        assertTrue(Urls.validateURL("https://localhost:80/"));
    }

    @Test
    public void test_validUrl_LocalIpWithPort() {
        assertTrue(Urls.validateURL("https://123.456.789.1:8080/"));
    }

    @Test
    public void test_validUrl_LocalWithTopLabel() {
        assertTrue(Urls.validateURL("https://localhost.com"));
    }

    @Test
    public void test_validUrl_LocalWithPortAndTopLabel() {
        assertTrue(Urls.validateURL("https://localhost.com:9090/"));
    }

    @Test
    public void test_validUrl_Remote() {
        assertTrue(Urls.validateURL("https://github.com/google//blob/master/UserGuide.md"));
    }

    @Test
    public void test_validUrl_Remote_WithParameter() {
        assertFalse(Urls.validateURL("https://github.org/wiki/GRUB_2?rd=Grub2"));
    }

    @Test
    public void test_validUrl_Remote_WithSpecialChar() {
        assertTrue(Urls.validateURL(
            "https://jenkins.org/blue/organizations/jenkins/zos%2Foauth-apis-client/,();$!a+bc=d:@&!*xy/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_buildUrl_UrlBlank() {
        Urls.buildURL(null, null, null);
    }

    @Test
    public void test_buildUrl_OtherBlank() {
        assertEquals("https://github.org/", Urls.buildURL("https://github.org/", null, null));
    }

    @Test
    public void test_buildUrl_FragmentBlank() {
        assertEquals("https://github.org/?abc=xyz", Urls.buildURL("https://github.org/", "abc=xyz", null));
    }

    @Test
    public void test_buildUrl_QueryBlank() {
        assertEquals("https://github.org#section2.3", Urls.buildURL("https://github.org", null, "section2.3"));
    }

    @Test(expected = NullPointerException.class)
    public void test_combinePath_Null() {
        assertEquals("/api/test", Urls.combinePath((String[]) null));
    }

    @Test
    public void test_combinePath() {
        assertEquals("/api/test", Urls.combinePath("api", "test"));
        assertEquals("/api/test", Urls.combinePath("api", "/test"));
        assertEquals("/api/test", Urls.combinePath("/api", "test"));
        assertEquals("/api/test", Urls.combinePath("/api", "/test"));
        assertEquals("/api/test", Urls.combinePath("/api/", "/test"));
        assertEquals("/api/test", Urls.combinePath("/api/", "//test"));
        assertEquals("/api/test", Urls.combinePath("//api/", "//test"));
    }

}
