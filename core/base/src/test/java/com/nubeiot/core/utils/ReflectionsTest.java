package com.nubeiot.core.utils;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.JsonObject;

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
        final ReflectionMockObjects.MockReflection mock = new ReflectionMockObjects.MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("getId");
        Reflections.executeMethod(mock, method, new JsonObject(), null);
    }

    @Test
    public void test_execute_method() throws NoSuchMethodException {
        final ReflectionMockObjects.MockReflection mock = new ReflectionMockObjects.MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("setName", String.class);
        Reflections.executeMethod(mock, method, "xxx", Void.class);
        Assert.assertEquals("xxx", mock.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_execute_method_noArgument() throws NoSuchMethodException {
        final ReflectionMockObjects.MockReflection mock = new ReflectionMockObjects.MockReflection("abc");
        final Method method = mock.getClass().getDeclaredMethod("methodNoArgument");
        Reflections.executeMethod(mock, method, "xxx", Void.class);
    }

    @Test(expected = NubeException.class)
    public void test_execute_method_throwNubeException() throws NoSuchMethodException {
        final ReflectionMockObjects.MockReflection mock = new ReflectionMockObjects.MockReflection("abc");
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
        final ReflectionMockObjects.MockReflection mock = new ReflectionMockObjects.MockReflection("abc");
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

    @Test
    public void test_get_mock_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = Reflections.scanClassesInPackage("com.nubeiot.core.utils",
                                                                                 ClusterDelegate.class,
                                                                                 IClusterDelegate.class);
        Assert.assertEquals(1, classes.size());
        final IClusterDelegate delegate = Reflections.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.IGNITE, delegate.getTypeName());
    }

    @Test
    public void test_get_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = Reflections.scanClassesInPackage("com.nubeiot.core.cluster",
                                                                                 ClusterDelegate.class,
                                                                                 IClusterDelegate.class);
        Assert.assertEquals(1, classes.size());
        final IClusterDelegate delegate = Reflections.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.HAZELCAST, delegate.getTypeName());
    }

    @Test
    public void test_assert_data_type_with_primitive() {
        Assert.assertTrue(Reflections.assertDataType(int.class, int.class));
        Assert.assertTrue(Reflections.assertDataType(int.class, Integer.class));
        Assert.assertTrue(Reflections.assertDataType(Integer.class, int.class));
        Assert.assertTrue(Reflections.assertDataType(Integer.class, Integer.class));
    }

    @Test
    public void test_assert_data_type() {
        Assert.assertFalse(Reflections.assertDataType(ReflectionMockObjects.MockParent.class,
                                                      ReflectionMockObjects.MockChild.class));
        Assert.assertTrue(Reflections.assertDataType(ReflectionMockObjects.MockChild.class,
                                                     ReflectionMockObjects.MockParent.class));
    }

}