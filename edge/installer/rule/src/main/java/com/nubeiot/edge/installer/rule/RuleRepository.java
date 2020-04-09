package com.nubeiot.edge.installer.rule;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.edge.installer.model.type.ModuleType;
import com.nubeiot.edge.installer.model.type.VertxModuleType;

import lombok.NonNull;

public final class RuleRepository extends AbstractLocalCache<ModuleType, ApplicationRule, RuleRepository>
    implements LocalDataCache<ModuleType, ApplicationRule> {

    public static RuleRepository createJVMRule(@NonNull String... artifactGroups) {
        final ApplicationRule rule = ApplicationRule.jvmRule(artifactGroups);
        return new RuleRepository().add(VertxModuleType.JAVA, rule)
                                   .add(VertxModuleType.GROOVY, rule)
                                   .add(VertxModuleType.KOTLIN, rule)
                                   .add(VertxModuleType.SCALA, rule);
    }

    @Override
    protected @NonNull String keyLabel() {
        return ModuleType.class.getName();
    }

    @Override
    protected @NonNull String valueLabel() {
        return ApplicationRule.class.getName();
    }

    @Override
    public RuleRepository add(@NonNull ModuleType key, @NonNull ApplicationRule applicationRule) {
        cache().put(key, applicationRule);
        return this;
    }

}
