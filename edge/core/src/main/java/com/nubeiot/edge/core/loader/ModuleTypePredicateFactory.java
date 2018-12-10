package com.nubeiot.edge.core.loader;

import java.util.List;
import java.util.stream.Collectors;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ModuleTypePredicateFactory {

    static ModuleTypePredicate factory(ModuleType moduleType, List<String> searchPattern) {
        List<String> patterns = searchPattern.stream().filter(Strings::isNotBlank).collect(Collectors.toList());
        if (ModuleType.JAVASCRIPT == moduleType) {
            return new ModuleTypePredicate.JavascriptPredicate(patterns);
        }

        if (ModuleType.GROOVY == moduleType) {
            return new ModuleTypePredicate.GroovyPredicate(patterns);
        }

        if (ModuleType.SCALA == moduleType) {
            return new ModuleTypePredicate.ScalaPredicate(patterns);
        }

        if (ModuleType.KOTLIN == moduleType) {
            return new ModuleTypePredicate.KotlinPredicate(patterns);
        }

        if (ModuleType.RUBY == moduleType) {
            return new ModuleTypePredicate.RubyPredicate(patterns);
        }

        return new ModuleTypePredicate.JavaPredicate(patterns);
    }

}
