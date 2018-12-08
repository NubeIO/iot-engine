package com.nubeiot.edge.core.loader;

import java.util.List;
import java.util.stream.Collectors;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

//TODO add more type
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ModuleTypePredicateFactory {

    static ModuleTypePredicate factory(ModuleType moduleType, List<String> searchPattern) {
        List<String> patterns = searchPattern.stream().filter(Strings::isNotBlank).collect(Collectors.toList());
        if (ModuleType.JAVASCRIPT == moduleType) {
            return new ModuleTypePredicate.JavascriptPredicate(patterns);
        }
        return new ModuleTypePredicate.JavaPredicate(patterns);
    }

}
