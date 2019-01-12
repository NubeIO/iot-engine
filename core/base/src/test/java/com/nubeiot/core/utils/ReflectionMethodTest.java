package com.nubeiot.core.utils;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;
import com.nubeiot.core.utils.mock.MockReflection;

public class ReflectionMethodTest {

    @Test(expected = NullPointerException.class)
    public void test_execute_method_instance_null() {
        ReflectionMethod.executeMethod(null, null, JsonObject.class, Collections.singletonList(String.class), "s");
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_method_null() {
        ReflectionMethod.executeMethod("", null, JsonObject.class, Collections.singletonList(String.class), "s");
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_output_null() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("getId");
        ReflectionMethod.executeMethod(mock, method, null, Collections.singleton(JsonObject.class), new JsonObject());
    }

    @Test
    public void test_execute_method() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("setName", String.class);
        ReflectionMethod.executeMethod(mock, method, Void.class, String.class, "xxx");
        Assert.assertEquals("xxx", mock.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_execute_method_noArgument() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("methodNoArgument");
        ReflectionMethod.executeMethod(mock, method, Void.class, String.class, "xxx");
    }

    @Test(expected = NubeException.class)
    public void test_execute_method_throwNubeException() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("throwNubeException", String.class);
        try {
            ReflectionMethod.executeMethod(mock, method, Void.class, Collections.singletonList(String.class), "hey");
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
            ReflectionMethod.executeMethod(mock, method, Void.class, Collections.singletonList(String.class), "hey");
        } catch (NubeException e) {
            Assert.assertNull(e.getMessage());
            Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, e.getErrorCode());
            Assert.assertEquals("hey", e.getCause().getMessage());
            throw e.getCause();
        }
    }

}
