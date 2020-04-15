package io.github.zero.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero.utils.Reflections.ReflectionField;
import io.github.zero.utils.Reflections.ReflectionMethod;
import io.github.zero.utils.mock.MockChild;
import io.github.zero.utils.mock.MockParent;

public class ReflectionsTest {

    public static final Function<Predicate<Method>, List<Method>> MF = p -> ReflectionMethod.find(MockParent.class, p);
    public static final Function<Predicate<Field>, List<Field>> FF = p -> ReflectionField.find(MockChild.class, p);

    @Test
    public void test_findMethod_has_modifier() {
        findMethodsHasModifier(5, Modifier.PUBLIC);
        findMethodsHasModifier(3, Modifier.PUBLIC, Modifier.STATIC);
        findMethodsHasModifier(2, Modifier.PUBLIC, Modifier.FINAL);
        findMethodsHasModifier(1, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        findMethodsHasModifier(2, Modifier.PRIVATE);
        findMethodsHasModifier(1, Modifier.PROTECTED);
        findMethodsHasModifier(0, Modifier.PROTECTED, Modifier.STATIC);
        findMethodsHasModifier(2, Modifier.STATIC, Modifier.FINAL);
        findMethodsHasModifier(0, Modifier.PUBLIC, Modifier.PRIVATE);
    }

    @Test
    public void test_findMethod_no_modifier() {
        findMethodsNoModifier(4, Modifier.PUBLIC);
        findMethodsNoModifier(6, Modifier.PUBLIC, Modifier.STATIC);
        findMethodsNoModifier(7, Modifier.PUBLIC, Modifier.FINAL);
        findMethodsNoModifier(8, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        findMethodsNoModifier(8, Modifier.PRIVATE, Modifier.STATIC);
        findMethodsNoModifier(7, Modifier.PRIVATE);
        findMethodsNoModifier(8, Modifier.PROTECTED);
        findMethodsNoModifier(9, Modifier.PROTECTED, Modifier.STATIC);
    }

    @Test
    public void test_findMethod_has_n_no_modifier() {
        assertModifiers(2, msg(Modifier.PUBLIC, Modifier.STATIC),
                        Functions.and(has(Modifier.PUBLIC), not(Modifier.STATIC)), MF);
        assertModifiers(2, msg(to(Modifier.PUBLIC, Modifier.STATIC), Modifier.FINAL),
                        Functions.and(has(Modifier.PUBLIC, Modifier.STATIC), not(Modifier.FINAL)), MF);
        assertModifiers(1, msg(to(Modifier.PUBLIC, Modifier.FINAL), Modifier.STATIC),
                        Functions.and(has(Modifier.PUBLIC, Modifier.FINAL), not(Modifier.STATIC)), MF);
    }

    @Test
    public void test_findField_has_modifier() {
        findFieldsHasModifier(4, Modifier.PUBLIC);
        findFieldsHasModifier(2, Modifier.PUBLIC, Modifier.STATIC);
        findFieldsHasModifier(2, Modifier.PUBLIC, Modifier.FINAL);
        findFieldsHasModifier(1, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        findFieldsHasModifier(4, Modifier.PRIVATE);
        findFieldsHasModifier(4, Modifier.PROTECTED);
        findFieldsHasModifier(2, Modifier.PROTECTED, Modifier.STATIC);
        findFieldsHasModifier(4, Modifier.STATIC, Modifier.FINAL);
        findFieldsHasModifier(8, Modifier.STATIC);
        findFieldsHasModifier(8, Modifier.FINAL);
        findFieldsHasModifier(0, Modifier.PUBLIC, Modifier.PRIVATE);
    }

    @Test
    public void test_findField_no_modifier() {
        findFieldsNoModifier(12, Modifier.PUBLIC);
        findFieldsNoModifier(14, Modifier.PUBLIC, Modifier.STATIC);
        findFieldsNoModifier(14, Modifier.PUBLIC, Modifier.FINAL);
        findFieldsNoModifier(15, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        findFieldsNoModifier(14, Modifier.PRIVATE, Modifier.STATIC);
        findFieldsNoModifier(12, Modifier.PRIVATE);
        findFieldsNoModifier(12, Modifier.PROTECTED);
        findFieldsNoModifier(14, Modifier.PROTECTED, Modifier.STATIC);
    }

    @Test
    public void test_findField_has_n_no_modifier() {
        assertModifiers(2, msg(Modifier.PUBLIC, Modifier.STATIC),
                        Functions.and(has(Modifier.PUBLIC), not(Modifier.STATIC)), FF);
        assertModifiers(1, msg(to(Modifier.PUBLIC, Modifier.STATIC), Modifier.FINAL),
                        Functions.and(has(Modifier.PUBLIC, Modifier.STATIC), not(Modifier.FINAL)), FF);
        assertModifiers(1, msg(to(Modifier.PUBLIC, Modifier.FINAL), Modifier.STATIC),
                        Functions.and(has(Modifier.PUBLIC, Modifier.FINAL), not(Modifier.STATIC)), FF);
    }

    private void findMethodsNoModifier(int expectedSize, int... modifiers) {
        assertModifiers(expectedSize, notMsg(modifiers), not(modifiers), MF);
    }

    private void findMethodsHasModifier(int expectedSize, int... modifiers) {
        assertModifiers(expectedSize, hasMsg(modifiers), has(modifiers), MF);
    }

    private void findFieldsNoModifier(int expectedSize, int... modifiers) {
        assertModifiers(expectedSize, notMsg(modifiers), not(modifiers), FF);
    }

    private void findFieldsHasModifier(int expectedSize, int... modifiers) {
        assertModifiers(expectedSize, hasMsg(modifiers), has(modifiers), FF);
    }

    private <T extends Member> void assertModifiers(int expectedSize, String msg, Predicate<T> predicate,
                                                    Function<Predicate<T>, List<T>> func) {
        List<T> list = func.apply(Functions.and(predicate, t -> !t.getName().contains("jacoco")));
        System.out.println(list);
        Assert.assertEquals(msg, expectedSize, list.size());
    }

    private String hasMsg(int... modifiers) {
        return "Has Modifiers: " +
               Arrays.stream(modifiers).mapToObj(Modifier::toString).collect(Collectors.joining(" "));
    }

    private String notMsg(int... modifiers) {
        return "Not Modifiers: " +
               Arrays.stream(modifiers).mapToObj(Modifier::toString).collect(Collectors.joining(" "));
    }

    private String msg(int[] has, int[] not) {
        return hasMsg(has) + " | " + notMsg(not);
    }

    private String msg(int has, int not) {
        return hasMsg(has) + " | " + notMsg(not);
    }

    private String msg(int[] has, int not) {
        return hasMsg(has) + " | " + notMsg(not);
    }

    private String msg(int has, int[] not) {
        return hasMsg(has) + " | " + notMsg(not);
    }

    private int[] to(int... modifiers) {
        return Arrays.stream(modifiers).toArray();
    }

    private <T extends Member> Predicate<T> has(int... modifiers) {
        return Reflections.hasModifiers(modifiers);
    }

    private <T extends Member> Predicate<T> not(int... modifiers) {
        return Reflections.notModifiers(modifiers);
    }

}
