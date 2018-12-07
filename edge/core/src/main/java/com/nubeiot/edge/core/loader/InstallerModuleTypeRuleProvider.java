package com.nubeiot.edge.core.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class InstallerModuleTypeRuleProvider extends ModuleTypeRuleProvider {

    @Override
    public ModuleTypeRule getModuleTypeRule() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA, "com.nubeiot.edge.service", this.validateGroup(ModuleType.JAVA));
    }

    @Override
    public List<String> getSupportGroups(ModuleType moduleType) {
      //put module type to support further module
        if (ModuleType.JAVA.equals(moduleType)) {
                return Arrays.asList("com.nubeio.edge.connector", "com.nubeio.edge.rule");
        } else {
                return new ArrayList<String>();
        }
    }

}
