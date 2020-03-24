package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.loader.VertxModuleType;

final class EdgeBiosRuleProvider implements Supplier<ModuleTypeRule> {

    @Override
    public ModuleTypeRule get() {
        return new ModuleTypeRule().registerRule(VertxModuleType.JAVA, Collections.singletonList("com.nubeiot.edge.module"));
    }

}
