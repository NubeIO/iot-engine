package com.nubeiot.edge.bios.installer;

import java.util.Arrays;
import java.util.function.Supplier;

import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;

final class InstallerModuleTypeRuleProvider implements Supplier<ModuleTypeRule> {

    @Override
    public ModuleTypeRule get() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA,
                                                 Arrays.asList("com.nubeio.edge.connector", "com.nubeio.edge.rule"));
    }

}
