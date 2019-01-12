package com.nubeiot.core.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.mock.MockChild;
import com.nubeiot.core.utils.mock.MockParent;

public class ReflectionClassTest {

    @Test
    public void test_get_mock_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = ReflectionClass.find("com.nubeiot.core.utils", IClusterDelegate.class,
                                                                     ClusterDelegate.class);
        Assert.assertEquals(1, classes.size());
        IClusterDelegate delegate = ReflectionClass.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.IGNITE, delegate.getTypeName());
    }

    @Test
    public void test_get_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = ReflectionClass.find("com.nubeiot.core.cluster", IClusterDelegate.class,
                                                                     ClusterDelegate.class);
        Assert.assertEquals(1, classes.size());
        IClusterDelegate delegate = ReflectionClass.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.HAZELCAST, delegate.getTypeName());
    }

    @Test
    public void test_assert_data_type_with_primitive() {
        Assert.assertTrue(ReflectionClass.assertDataType(int.class, int.class));
        Assert.assertTrue(ReflectionClass.assertDataType(int.class, Integer.class));
        Assert.assertTrue(ReflectionClass.assertDataType(Integer.class, int.class));
        Assert.assertTrue(ReflectionClass.assertDataType(Integer.class, Integer.class));
    }

    @Test
    public void test_assert_data_type() {
        Assert.assertFalse(ReflectionClass.assertDataType(MockParent.class, MockChild.class));
        Assert.assertTrue(ReflectionClass.assertDataType(MockChild.class, MockParent.class));
    }

}
