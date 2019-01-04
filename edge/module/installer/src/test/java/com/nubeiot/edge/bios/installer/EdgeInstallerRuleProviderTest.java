package com.nubeiot.edge.bios.installer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;

public class EdgeInstallerRuleProviderTest {

    private ModuleTypeRule rule;

    @Before
    public void setup() {
        this.rule = new EdgeInstallerRuleProvider().get();
    }

    @Test
    public void test_ModuleTypeJAVA_success() {
        ModuleTypeRule rule = new EdgeInstallerRuleProvider().get();
        assertTrue(rule.getRule(ModuleType.JAVA).test("com.nubeiot.edge.connector.xyz"));
        assertTrue(rule.getSearchPattern(ModuleType.JAVA)
                       .containsAll(Arrays.asList("com.nubeiot.edge.connector", "com.nubeiot.edge.rule")));
    }

    @Test
    public void test_ModuleTypeJAVA_failed() {
        assertFalse(rule.getRule(ModuleType.JAVA).test("com.nubeiot.edge.ccc.xyz"));
    }

    @Test
    public void test_ModuleTypeJAVAScript() {
        assertFalse(rule.getRule(ModuleType.JAVASCRIPT).test("olala"));
    }

}
