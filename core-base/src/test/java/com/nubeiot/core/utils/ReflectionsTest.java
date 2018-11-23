package com.nubeiot.core.utils;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class ReflectionsTest {

    @Test(expected = NullPointerException.class)
    public void test_execute_method_instance_null() {
        Reflections.executeMethod(null, null, new JsonObject(), JsonObject.class);
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_method_null() {
        Reflections.executeMethod("", null, new JsonObject(), JsonObject.class);
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_output_null() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("getId");
        Reflections.executeMethod(mock, method, new JsonObject(), null);
    }

    @Test
    public void test_execute_method() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("setName", String.class);
        Reflections.executeMethod(mock, method, "xxx", Void.class);
        Assert.assertEquals("xxx", mock.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_execute_method_noArgument() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("methodNoArgument");
        Reflections.executeMethod(mock, method, "xxx", Void.class);
    }

    @Test(expected = NubeException.class)
    public void test_execute_method_throwNubeException() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("throwNubeException", String.class);
        try {
            Reflections.executeMethod(mock, method, "hey", Void.class);
        } catch (NubeException e) {
            Assert.assertEquals("hey", e.getMessage());
            Assert.assertEquals(NubeException.ErrorCode.SERVICE_ERROR, e.getErrorCode());
            Assert.assertNull(e.getCause());
            throw e;
        }
    }

    @Test(expected = RuntimeException.class)
    public void test_execute_method_throwUnknownException() throws Throwable {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("throwUnknownException", String.class);
        try {
            Reflections.executeMethod(mock, method, "hey", Void.class);
        } catch (NubeException e) {
            Assert.assertNull(e.getMessage());
            Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, e.getErrorCode());
            Assert.assertEquals("hey", e.getCause().getMessage());
            throw e.getCause();
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class MockReflection {

        private final String id;
        @Setter
        private String name;

        public int methodNoArgument() {
            return 1;
        }

        public void throwNubeException(String hey) {
            throw new ServiceException(hey);
        }

        public void throwUnknownException(String hey) {
            throw new RuntimeException(hey);
        }

    }

}