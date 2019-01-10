package com.nubeiot.core.exceptions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NubeExceptionConverterTest {

    private NubeExceptionConverter converter;

    @Before
    public void setup() {
        converter = new NubeExceptionConverter(true, null);
    }

    @Test(expected = NullPointerException.class)
    public void test_null() {
        converter.apply(null);
    }

    @Test
    public void test_only_code() {
        NubeException t = converter.apply(new NubeException(NubeException.ErrorCode.SERVICE_ERROR));
        Assert.assertEquals(NubeException.ErrorCode.SERVICE_ERROR, t.getErrorCode());
        Assert.assertEquals("SERVICE_ERROR", t.getMessage());
        Assert.assertNull(t.getCause());
    }

    @Test
    public void test_only_message() {
        NubeException t = converter.apply(new NubeException("2"));
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, t.getErrorCode());
        Assert.assertEquals("2", t.getMessage());
        Assert.assertNull(t.getCause());
    }

    @Test(expected = IllegalStateException.class)
    public void test_only_throwable_has_message() throws Throwable {
        NubeException t = converter.apply(new NubeException(new IllegalStateException("lorem")));
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, t.getErrorCode());
        Assert.assertEquals("UNKNOWN_ERROR | Cause: lorem", t.getMessage());
        throw t.getCause();
    }

    @Test(expected = IllegalStateException.class)
    public void test_only_throwable_no_message() throws Throwable {
        NubeException t = converter.apply(new NubeException(new IllegalStateException()));
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, t.getErrorCode());
        Assert.assertEquals("UNKNOWN_ERROR", t.getMessage());
        throw t.getCause();
    }

    @Test
    public void test_with_code_without_cause() {
        NubeException t = converter.apply(new NubeException(NubeException.ErrorCode.EVENT_ERROR, "1"));
        Assert.assertEquals(NubeException.ErrorCode.EVENT_ERROR, t.getErrorCode());
        Assert.assertEquals("1", t.getMessage());
        Assert.assertNull(t.getCause());
    }

    @Test(expected = RuntimeException.class)
    public void test_with_code_with_other_cause_no_message() throws Throwable {
        NubeException t = converter.apply(
            new NubeException(NubeException.ErrorCode.EVENT_ERROR, "1", new RuntimeException()));
        Assert.assertEquals(NubeException.ErrorCode.EVENT_ERROR, t.getErrorCode());
        Assert.assertEquals("1", t.getMessage());
        throw t.getCause();
    }

    @Test(expected = RuntimeException.class)
    public void test_with_code_with_other_cause_has_message() throws Throwable {
        NubeException t = converter.apply(
            new NubeException(NubeException.ErrorCode.HTTP_ERROR, "abc", new RuntimeException("xyz")));
        Assert.assertEquals(NubeException.ErrorCode.HTTP_ERROR, t.getErrorCode());
        Assert.assertEquals("abc | Cause: xyz", t.getMessage());
        throw t.getCause();
    }

    @Test(expected = NubeException.class)
    public void test_with_code_with_nube_cause() throws Throwable {
        NubeException t = converter.apply(new SecurityException("abc", new EngineException("xyz")));
        Assert.assertEquals(NubeException.ErrorCode.SECURITY_ERROR, t.getErrorCode());
        Assert.assertEquals("abc | Cause: xyz - Error Code: ENGINE_ERROR", t.getMessage());
        throw t.getCause();
    }

    @Test(expected = HiddenException.class)
    public void test_with_code_with_hidden_cause() throws Throwable {
        NubeException t = converter.apply(
            new ServiceException("abc", new HiddenException(NubeException.ErrorCode.EVENT_ERROR, "xyz")));
        Assert.assertEquals(NubeException.ErrorCode.SERVICE_ERROR, t.getErrorCode());
        Assert.assertEquals("abc", t.getMessage());
        NubeException cause = (NubeException) t.getCause();
        Assert.assertEquals(NubeException.ErrorCode.EVENT_ERROR, cause.getErrorCode());
        Assert.assertEquals("xyz", cause.getMessage());
        throw t.getCause();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_other_exception_no_message() throws Throwable {
        NubeException t = converter.apply(new IllegalArgumentException());
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, t.getErrorCode());
        Assert.assertEquals("UNKNOWN_ERROR", t.getMessage());
        throw t.getCause();
    }

    @Test(expected = RuntimeException.class)
    public void test_other_exception_has_message() throws Throwable {
        NubeException t = converter.apply(new RuntimeException("xyz"));
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, t.getErrorCode());
        Assert.assertEquals("UNKNOWN_ERROR | Cause: xyz", t.getMessage());
        throw t.getCause();
    }

    @Test
    public void test_other_exception_bound_nube_exception() {
        NubeException t = converter.apply(new RuntimeException(new ServiceException("hey")));
        Assert.assertEquals(NubeException.ErrorCode.SERVICE_ERROR, t.getErrorCode());
        Assert.assertEquals("hey", t.getMessage());
        Assert.assertNull(t.getCause());
    }

}
