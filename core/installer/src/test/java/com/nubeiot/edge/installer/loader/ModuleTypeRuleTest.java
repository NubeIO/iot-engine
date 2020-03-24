package com.nubeiot.edge.installer.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

// TODO mock and test with different language https://github.com/NubeIO/iot-engine/issues/239
public class ModuleTypeRuleTest {

    @Test
    public void testRule() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(VertxModuleType.JAVA,
                                                                          Collections.singletonList(""));
        assertFalse(moduleTypeRule.getRule(VertxModuleType.JAVA).test(null));
        assertFalse(moduleTypeRule.getRule(VertxModuleType.JAVA).test(""));
    }

    @Test
    public void testOneGroup() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(VertxModuleType.JAVA,
                                                                          Collections.singletonList(
                                                                              "com.nubeio.edge.connector"));
        assertTrue(moduleTypeRule.getRule(VertxModuleType.JAVA).test("com.nubeio.edge.connector"));
        final List<String> searchPattern = moduleTypeRule.getSearchPattern(VertxModuleType.JAVA);
        assertEquals(1, searchPattern.size());
        assertEquals("com.nubeio.edge.connector", searchPattern.get(0));
    }

    @Test
    public void testManyGroups() {
        List<String> groups = Arrays.asList("group1", "group2", "group3");
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(VertxModuleType.JAVA, groups);
        Predicate<String> javaRule = moduleTypeRule.getRule(VertxModuleType.JAVA);
        groups.forEach(item -> assertTrue(javaRule.test(item)));
        assertFalse(javaRule.test("group4"));
    }

    @Test
    public void testDifferentModuleType() {
        final List<String> javaSearchPattern = Arrays.asList("group1", "group2");
        final List<String> jsSearchPattern = Arrays.asList("group3", "group4");
        ModuleTypeRule rule = new ModuleTypeRule().registerRule(VertxModuleType.JAVA, javaSearchPattern)
                                                  .registerRule(VertxModuleType.JAVASCRIPT, jsSearchPattern);
        assertTrue(rule.getRule(VertxModuleType.JAVA).test("group1.abc"));
        assertEquals(javaSearchPattern, rule.getSearchPattern(VertxModuleType.JAVA));
        assertTrue(rule.getRule(VertxModuleType.JAVASCRIPT).test("abc"));
        assertEquals(jsSearchPattern, rule.getSearchPattern(VertxModuleType.JAVASCRIPT));
        assertFalse(rule.getRule(VertxModuleType.GROOVY).test("xxx"));
        assertEquals(0, rule.getSearchPattern(VertxModuleType.GROOVY).size());
    }

}
