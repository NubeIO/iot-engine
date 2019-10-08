package com.nubeiot.edge.module.installer;

import java.util.Arrays;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;

final class ServiceInstallerRuleProvider implements Supplier<ModuleTypeRule> {

    @Override
    public ModuleTypeRule get() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA,
                                                 Arrays.asList("com.nubeiot.edge.connector", "com.nubeiot.edge.rule"));
    }

}
