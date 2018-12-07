package com.nubeiot.edge.core.loader;

import java.util.List;
import java.util.function.Predicate;

public abstract class ModuleTypeRuleProvider {
    public abstract ModuleTypeRule getModuleTypeRule();
    public abstract List<String> getSupportGroups(ModuleType moduleType);
    
    //consider to remove final -> overridable in derived classes
    protected final Predicate<String> validateGroup(ModuleType moduleType) {
        return artifact -> {
            List<String> supportGroups = this.getSupportGroups(moduleType);
            if (supportGroups == null || supportGroups.isEmpty()) {
                return true;
            }
            return supportGroups.stream().anyMatch(item -> artifact.startsWith(item));
        };
    }
}
