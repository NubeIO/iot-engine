package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.function.Supplier;

import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;

final class EdgeBiosRuleProvider implements Supplier<ModuleTypeRule> {

    @Override
    public ModuleTypeRule get() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA, Collections.singletonList("com.nubeiot.edge.module"));
    }

}
