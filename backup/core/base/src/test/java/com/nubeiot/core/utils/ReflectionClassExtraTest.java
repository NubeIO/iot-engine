package com.nubeiot.core.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero88.utils.Reflections.ReflectionClass;

import com.nubeiot.core.cluster.ClusterDelegate;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;

public class ReflectionClassExtraTest {

    @Test
    public void test_get_mock_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = ReflectionClass.stream("com.nubeiot.core.utils", IClusterDelegate.class,
                                                                       ClusterDelegate.class)
                                                               .collect(Collectors.toList());
        Assert.assertEquals(1, classes.size());
        IClusterDelegate delegate = ReflectionClass.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.IGNITE, delegate.getTypeName());
    }

    @Test
    public void test_get_classes_by_annotation() {
        List<Class<IClusterDelegate>> classes = ReflectionClass.stream("com.nubeiot.core.cluster",
                                                                       IClusterDelegate.class, ClusterDelegate.class)
                                                               .collect(Collectors.toList());
        Assert.assertEquals(1, classes.size());
        IClusterDelegate delegate = ReflectionClass.createObject(classes.get(0));
        Assert.assertNotNull(delegate);
        Assert.assertEquals(ClusterType.HAZELCAST, delegate.getTypeName());
    }

}
