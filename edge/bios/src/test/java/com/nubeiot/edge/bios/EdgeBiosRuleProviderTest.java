package com.nubeiot.edge.bios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;

public class EdgeBiosRuleProviderTest {

    private ModuleTypeRule rule;

    @Before
    public void setup() {
        this.rule = new EdgeBiosRuleProvider().get();
    }

    @Test
    public void test_ModuleTypeJAVA_success() {
        assertTrue(rule.getRule(ModuleType.JAVA).test("com.nubeiot.edge.module.xyz"));
        final List<String> searchPattern = rule.getSearchPattern(ModuleType.JAVA);
        assertEquals(1, searchPattern.size());
        assertTrue(searchPattern.contains("com.nubeiot.edge.module"));
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
