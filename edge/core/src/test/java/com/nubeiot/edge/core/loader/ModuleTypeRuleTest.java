package com.nubeiot.edge.core.loader;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.Test;

public class ModuleTypeRuleTest {
    
    @Test
    public void testRule() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA, "", object -> !Objects.isNull(object));
        assertFalse(moduleTypeRule.getRule(ModuleType.JAVA).test(null));
        assertTrue(moduleTypeRule.getRule(ModuleType.JAVA).test(""));
    }
    
    @Test
    public void testOneGroup() {
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA, "com.nubeio.edge.connector", artifactId -> artifactId.startsWith("com.nubeio.edge.connector"));
        assertTrue(moduleTypeRule.getRule(ModuleType.JAVA).test("com.nubeio.edge.connector"));
        assertEquals("com.nubeio.edge.connector", moduleTypeRule.getSearchPattern(ModuleType.JAVA));
    }
    
    @Test
    public void testManyGroups() {
        List<String> groups = Arrays.asList("group1", "group2", "group3");
        ModuleTypeRule moduleTypeRule = new ModuleTypeRule().registerRule(ModuleType.JAVA, "group4", artifactId -> groups.stream().anyMatch(item -> artifactId.startsWith(item)));
        Predicate<String> javaRule = moduleTypeRule.getRule(ModuleType.JAVA);
        groups.forEach(item -> assertTrue(javaRule.test(item)));
        assertFalse(javaRule.test("group4"));
    }
}