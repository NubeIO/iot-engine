package com.nubeiot.edge.installer.loader;

import java.util.List;
import java.util.stream.Collectors;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ModuleTypePredicateFactory {

    static ModuleTypePredicate factory(ModuleType moduleType, List<String> searchPattern) {
        List<String> patterns = searchPattern.stream().filter(Strings::isNotBlank).collect(Collectors.toList());
        if (VertxModuleType.JAVASCRIPT == moduleType) {
            return new ModuleTypePredicate.JavascriptPredicate(patterns);
        }

        if (VertxModuleType.GROOVY == moduleType) {
            return new ModuleTypePredicate.GroovyPredicate(patterns);
        }

        if (VertxModuleType.SCALA == moduleType) {
            return new ModuleTypePredicate.ScalaPredicate(patterns);
        }

        if (VertxModuleType.KOTLIN == moduleType) {
            return new ModuleTypePredicate.KotlinPredicate(patterns);
        }

        if (VertxModuleType.RUBY == moduleType) {
            return new ModuleTypePredicate.RubyPredicate(patterns);
        }

        return new ModuleTypePredicate.JavaPredicate(patterns);
    }

}
