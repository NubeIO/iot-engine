package com.nubeiot.edge.core.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import io.vertx.core.shareddata.Shareable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleTypeRule implements Shareable {

    private final Map<ModuleType, ModuleTypePredicate> rules;

    public ModuleTypeRule() {
        rules = new HashMap<>();
    }

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

    private Map<ModuleType, ModuleTypePredicate> rules() {
        return Collections.unmodifiableMap(this.rules);
    }

    @Override
    public Shareable copy() {
        return new ModuleTypeRule(this.rules());
    }

}
