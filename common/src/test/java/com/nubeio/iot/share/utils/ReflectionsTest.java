package com.nubeio.iot.share.utils;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

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

    @Getter
    @RequiredArgsConstructor
    private static class MockReflection {

        private final String id;
        @Setter
        private String name;

    }

}