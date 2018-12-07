package com.nubeiot.edge.core.loader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class InstallerModuleTypeRuleProviderTest {
    @Test
    public void testModuleTypeJAVA() {
        InstallerModuleTypeRuleProvider provider = new InstallerModuleTypeRuleProvider();
        List<String> supportGroups = provider.getSupportGroups(ModuleType.JAVA);
        supportGroups.forEach(item -> {
            assertTrue(provider.validateGroup(ModuleType.JAVA).test(item));
        });
    }
    
    @Test
    public void testModuleTypeJS() {
        InstallerModuleTypeRuleProvider provider = new InstallerModuleTypeRuleProvider();
        List<String> supportGroups = provider.getSupportGroups(ModuleType.JAVASCRIPT);
        supportGroups.forEach(item -> {
            assertTrue(provider.validateGroup(ModuleType.JAVASCRIPT).test(item));
        });
    }
     
    @Test
    public void testGroupTrue() {
        InstallerModuleTypeRuleProvider provider = new InstallerModuleTypeRuleProvider();
        assertTrue(provider.validateGroup(ModuleType.JAVA).test("com.nubeio.edge.rule"));
        assertTrue(provider.validateGroup(ModuleType.JAVA).test("com.nubeio.edge.connector"));
    }
    
    @Test
    public void testGroupFalse() {
        InstallerModuleTypeRuleProvider provider = new InstallerModuleTypeRuleProvider();
        assertFalse(provider.validateGroup(ModuleType.JAVA).test("group1"));
    }
}
