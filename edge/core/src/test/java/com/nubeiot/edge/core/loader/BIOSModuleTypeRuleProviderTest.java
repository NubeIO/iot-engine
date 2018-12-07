package com.nubeiot.edge.core.loader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class BIOSModuleTypeRuleProviderTest {

    @Test
    public void testModuleTypeJAVA() {
        BIOSModuleTypeRuleProvider provider = new BIOSModuleTypeRuleProvider();
        List<String> supportGroups = provider.getSupportGroups(ModuleType.JAVA);
        supportGroups.forEach(item -> {
            assertTrue(provider.validateGroup(ModuleType.JAVA).test(item));
        });
    }
    
    @Test
    public void testModuleTypeJS() {
        BIOSModuleTypeRuleProvider provider = new BIOSModuleTypeRuleProvider();
        List<String> supportGroups = provider.getSupportGroups(ModuleType.JAVASCRIPT);
        supportGroups.forEach(item -> {
            assertTrue(provider.validateGroup(ModuleType.JAVASCRIPT).test(item));
        });
    }
    
    @Test
    public void testGroupTrue() {
        BIOSModuleTypeRuleProvider provider = new BIOSModuleTypeRuleProvider();
        assertTrue(provider.validateGroup(ModuleType.JAVA).test("com.nubeiot.edge.module"));
    }
    
    @Test
    public void testGroupFalse() {
        BIOSModuleTypeRuleProvider provider = new BIOSModuleTypeRuleProvider();
        assertFalse(provider.validateGroup(ModuleType.JAVA).test("group1"));
    }
    
}
