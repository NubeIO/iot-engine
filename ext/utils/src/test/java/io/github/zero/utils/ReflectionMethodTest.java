package io.github.zero.utils;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero.exceptions.ErrorCode;
import io.github.zero.exceptions.FileException;
import io.github.zero.exceptions.SneakyErrorCodeException;
import io.github.zero.utils.Reflections.ReflectionMethod;
import io.vertx.core.json.JsonObject;

public class ReflectionMethodTest {

    @Test(expected = NullPointerException.class)
    public void test_execute_method_instance_null() {
        ReflectionMethod.execute(null, null, JsonObject.class, Collections.singletonList(String.class), "s");
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_method_null() {
        ReflectionMethod.execute("", null, JsonObject.class, Collections.singletonList(String.class), "s");
    }

    @Test(expected = NullPointerException.class)
    public void test_execute_method_output_null() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("getId");
        ReflectionMethod.execute(mock, method, null, Collections.singleton(JsonObject.class), new JsonObject());
    }

    @Test
    public void test_execute_method() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("setName", String.class);
        ReflectionMethod.execute(mock, method, Void.class, String.class, "xxx");
        Assert.assertEquals("xxx", mock.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_execute_method_noArgument() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("methodNoArgument");
        ReflectionMethod.execute(mock, method, Void.class, String.class, "xxx");
    }

    @Test(expected = FileException.class)
    public void test_execute_method_throwSneakyException() throws NoSuchMethodException {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("throwSneakyException", String.class);
        try {
            ReflectionMethod.execute(mock, method, Void.class, Collections.singletonList(String.class), "hey");
        } catch (SneakyErrorCodeException e) {
            final SneakyErrorCodeException cause = (SneakyErrorCodeException) e.getCause();
            Assert.assertNotNull(cause);
            Assert.assertEquals("hey", cause.getMessage());
            Assert.assertEquals(ErrorCode.FILE_ERROR, cause.getErrorCode());
            throw cause;
        }
    }

    @Test(expected = RuntimeException.class)
    public void test_execute_method_throwUnknownException() throws Throwable {
        final MockReflection mock = new MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("throwUnknownException", String.class);
        try {
            ReflectionMethod.execute(mock, method, Void.class, Collections.singletonList(String.class), "hey");
        } catch (SneakyErrorCodeException e) {
            Assert.assertNull(e.getMessage());
            Assert.assertEquals(ErrorCode.REFLECTION_ERROR, e.getErrorCode());
            Assert.assertEquals("hey", e.getCause().getMessage());
            throw e.getCause();
        }
    }

}
