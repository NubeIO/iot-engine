package com.nubeiot.edge.module.installer;

import java.util.Arrays;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.loader.VertxModuleType;

final class ServiceInstallerRuleProvider implements Supplier<ModuleTypeRule> {

    @Override
    public ModuleTypeRule get() {
        return new ModuleTypeRule().registerRule(VertxModuleType.JAVA,
                                                 Arrays.asList("com.nubeiot.edge.connector", "com.nubeiot.edge.rule"));
    }

}
