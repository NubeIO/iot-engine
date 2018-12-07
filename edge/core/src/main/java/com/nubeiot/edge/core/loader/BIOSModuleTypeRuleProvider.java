package com.nubeiot.edge.core.loader;

import java.util.Arrays;
import java.util.List;


public final class BIOSModuleTypeRuleProvider extends ModuleTypeRuleProvider {

    @Override
    public ModuleTypeRule getModuleTypeRule() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA, "com.nubeiot.edge.module", this.validateGroup(ModuleType.JAVA));
    }

    @Override
    public List<String> getSupportGroups(ModuleType moduleType) {
        return Arrays.asList("com.nubeiot.edge.module");
    }

}
