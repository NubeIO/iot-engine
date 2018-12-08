package com.nubeiot.edge.core.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

// TODO mock and test with different language
public class ModuleTypeRuleTest {

    @Test
    public void testRule() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA,
                                                                          Collections.singletonList(""));
        assertFalse(moduleTypeRule.getRule(ModuleType.JAVA).test(null));
        assertFalse(moduleTypeRule.getRule(ModuleType.JAVA).test(""));
    }

    @Test
    public void testOneGroup() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA, Collections.singletonList(
                "com.nubeio.edge.connector"));
        assertTrue(moduleTypeRule.getRule(ModuleType.JAVA).test("com.nubeio.edge.connector"));
        final List<String> searchPattern = moduleTypeRule.getSearchPattern(ModuleType.JAVA);
        assertEquals(1, searchPattern.size());
        assertEquals("com.nubeio.edge.connector", searchPattern.get(0));
    }

    @Test
    public void testManyGroups() {
        List<String> groups = Arrays.asList("group1", "group2", "group3");
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA, groups);
        Predicate<String> javaRule = moduleTypeRule.getRule(ModuleType.JAVA);
        groups.forEach(item -> assertTrue(javaRule.test(item)));
        assertFalse(javaRule.test("group4"));
    }

}