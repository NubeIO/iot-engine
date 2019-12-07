package com.nubeiot.core.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.mock.MockChild;
import com.nubeiot.core.utils.mock.MockReflection;

public class ReflectionFieldTest {

    @Test
    public void test_null_predicate() {
        List<Field> fields = ReflectionField.find(MockReflection.class, null);
        Assert.assertTrue(fields.parallelStream().anyMatch(item -> "id".equals(item.getName())));
        Assert.assertTrue(fields.parallelStream().anyMatch(item -> "name".equals(item.getName())));
    }

    @Test
    public void test_non_null_predicate() {
        Stream<Field> fields = ReflectionField.stream(MockReflection.class, field -> "id".equals(field.getName()));
        Assert.assertTrue(fields.allMatch(item -> "id".equals(item.getName())));
    }

    @Test
    public void test_constant_by_name() {
        Object constantByName = ReflectionField.constantByName(MockChild.class, "F4");
        Assert.assertEquals(constantByName, MockChild.F4);
    }

    @Test
    public void test_constant_not_public() {
        Object constantByName = ReflectionField.constantByName(MockChild.class, "F1");
        Assert.assertNull(constantByName);
    }

    @Test
    public void test_constant_not_static() {
        Object constantByName = ReflectionField.constantByName(MockChild.class, "f4");
        Assert.assertNull(constantByName);
    }

    @Test
    public void test_constant_not_final() {
        Object constantByName = ReflectionField.constantByName(MockChild.class, "f42");
        Assert.assertNull(constantByName);
    }

    @Test
    public void test_field_not_found() {
        Assert.assertNull(ReflectionField.constantByName(MockChild.class, "f6"));
    }

    // @Test(expected = NubeException.class)
    // public void test_class_cast_exception() {
    // String constantByName = ReflectionField.constantByName(MockChild.class, "F4");
    // }

    @Test
    public void test_get_values_by_type() {
        MockChild mock = new MockChild();
        List<Integer> fieldValuesByType = ReflectionField.getFieldValuesByType(mock, Integer.class);
        Assert.assertFalse(fieldValuesByType.isEmpty());
    }

    @Test
    public void test_get_values_by_undefined_type() {
        MockChild mock = new MockChild();
        List<String> fieldValuesByType = ReflectionField.getFieldValuesByType(mock, String.class);
        Assert.assertTrue(fieldValuesByType.isEmpty());
    }

    @Test
    public void test_get_field_value() {
        MockChild mock = new MockChild();
        List<Field> fields = ReflectionField.find(MockChild.class, field -> "f1".equals(field.getName()));
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        Integer fieldValue = ReflectionField.getFieldValue(mock, fields.get(0), Integer.class);
        Assert.assertEquals(Integer.valueOf(0), fieldValue);
    }

    @Test
    public void test_get_constant_field_value() {
        MockChild mock = new MockChild();
        List<Field> fields = ReflectionField.find(MockChild.class, field -> "F4".equals(field.getName()));
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        Integer fieldValue = ReflectionField.getFieldValue(mock, fields.get(0), Integer.class);
        Assert.assertEquals(Integer.valueOf(0), fieldValue);
    }

    @Test
    public void test_get_null_field_value() {
        MockChild mock = new MockChild();
        List<Field> fields = ReflectionField.find(MockChild.class, field -> "f1".equals(field.getName()));
        Assert.assertNotNull(fields);
        Assert.assertEquals(1, fields.size());
        String fieldValue = ReflectionField.getFieldValue(mock, fields.get(0), String.class);
        Assert.assertNull(fieldValue);
    }

}
