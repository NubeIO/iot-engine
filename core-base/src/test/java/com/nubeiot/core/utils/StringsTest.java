package com.nubeiot.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;

import com.nubeiot.core.TestBase;

public class StringsTest extends TestBase {

    @Test
    public void test_Blank() {
        assertTrue(Strings.isBlank(""));
        assertTrue(Strings.isBlank(null));
        assertTrue(Strings.isBlank(" "));
        assertTrue(Strings.isBlank("    "));
        assertFalse(Strings.isBlank("a"));
    }

    @Test
    public void test_NotBlank() {
        assertTrue(Strings.isNotBlank("a"));
        assertTrue(Strings.isNotBlank(" a"));
        assertTrue(Strings.isNotBlank("a "));
        assertTrue(Strings.isNotBlank(" a "));
        assertFalse(Strings.isNotBlank(" "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_RequireNotBlank_shouldFailed() {
        Strings.requireNotBlank("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_RequireNotBlankWithMessage() {
        try {
            Strings.requireNotBlank("", "Cannot blank");
        } catch (IllegalArgumentException ex) {
            assertEquals("Cannot blank", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void test_RequireNotBlank_shouldSuccess() {
        assertEquals("abc", Strings.requireNotBlank(" abc "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_OptimizeNoSpace_BlankValue() {
        Strings.optimizeNoSpace("");
    }

    @Test
    public void test_OptimizeNoSpace_Trim_Success() {
        assertEquals("abc", Strings.optimizeNoSpace(" abc "));
    }

    @Test
    public void test_OptimizeNoSpace_Inside_Success() {
        assertEquals("abc", Strings.optimizeNoSpace(" a b c "));
    }

    @Test
    public void test_MinLength_Success() {
        assertEquals("abc", Strings.requiredMinLength(" abc ", 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_MinLength_Failed() {
        Strings.requiredMinLength(" a b ", 4);
    }

    @Test
    public void test_ConvertToInt_StringBlank() {
        assertEquals(2, Strings.convertToInt("", 2));
    }

    @Test
    public void test_ConvertToInt_StringNotNumber() {
        assertEquals(4, Strings.convertToInt(" a b ", 4));
    }

    @Test
    public void test_ConvertToInt_StringNotInt() {
        assertEquals(6, Strings.convertToInt("5.0", 6));
    }

    @Test
    public void test_ConvertToInt_Success() {
        assertEquals(8, Strings.convertToInt(" 8 ", 8));
    }

    @Test
    public void test_toString() {
        assertEquals("", Strings.toString(null));
        assertEquals("123", Strings.toString(123));
        assertEquals("true", Strings.toString(Boolean.TRUE));
        assertEquals("xy", Strings.toString(" xy    "));
    }

    @Test
    public void test_convert() {
        InputStream is = StringsTest.class.getClassLoader().getResourceAsStream("test.properties");
        assertEquals("zero.test=lalala", Strings.convertToString(is).trim());
    }

    @Test(expected = NullPointerException.class)
    public void test_convert_null() {
        Strings.convertToString(null);
    }

}
