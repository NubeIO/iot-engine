package com.nubeiot.edge.module.installer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.loader.VertxModuleType;

public class ServiceInstallerRuleProviderTest {

    private ModuleTypeRule rule;

    @Before
    public void setup() {
        this.rule = new ServiceInstallerRuleProvider().get();
    }

    @Test
    public void test_ModuleTypeJAVA_success() {
        ModuleTypeRule rule = new ServiceInstallerRuleProvider().get();
        assertTrue(rule.getRule(VertxModuleType.JAVA).test("com.nubeiot.edge.connector.xyz"));
        assertTrue(rule.getSearchPattern(VertxModuleType.JAVA)
                       .containsAll(Arrays.asList("com.nubeiot.edge.connector", "com.nubeiot.edge.rule")));
    }

    @Test
    public void test_ModuleTypeJAVA_failed() {
        assertFalse(rule.getRule(VertxModuleType.JAVA).test("com.nubeiot.edge.ccc.xyz"));
    }

    @Test
    public void test_ModuleTypeJAVAScript() {
        assertFalse(rule.getRule(VertxModuleType.JAVASCRIPT).test("olala"));
    }

}
