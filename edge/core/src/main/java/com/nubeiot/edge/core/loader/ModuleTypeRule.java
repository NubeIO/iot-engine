package com.nubeiot.edge.core.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class ModuleTypeRule {

    private final Map<ModuleType, ModuleTypePredicate> rules = new HashMap<>();

    public ModuleTypeRule registerRule(ModuleType moduleType, List<String> searchPattern) {
        rules.put(moduleType, ModuleTypePredicateFactory.factory(moduleType, searchPattern));
        return this;
    }

    public Predicate<String> getRule(ModuleType moduleType) {
        final ModuleTypePredicate ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? any -> false : ruleMetadata.getRule();
    }

    public List<String> getSearchPattern(ModuleType moduleType) {
        final ModuleTypePredicate ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? new ArrayList<>() : ruleMetadata.getSearchPattern();
    }

}
