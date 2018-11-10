package com.nubeio.iot.edge.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class ModuleTypeRule {

    private final Map<ModuleType, RuleMetadata> rules = new HashMap<>();

    public ModuleTypeRule registerRule(ModuleType moduleType, String searchPattern, Predicate<String> rule) {
        rules.put(moduleType, new RuleMetadata(searchPattern, rule));
        return this;
    }

    public Predicate<String> getRule(ModuleType moduleType) {
        final RuleMetadata ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? any -> true : ruleMetadata.getRule();
    }

    public String getSearchPattern(ModuleType moduleType) {
        final RuleMetadata ruleMetadata = this.rules.get(moduleType);
        return Objects.isNull(ruleMetadata) ? "" : ruleMetadata.getSearchPattern();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @Getter
    static class RuleMetadata {

        @NonNull
        @EqualsAndHashCode.Include
        private final String searchPattern;
        @NonNull
        private final Predicate<String> rule;

    }

}
