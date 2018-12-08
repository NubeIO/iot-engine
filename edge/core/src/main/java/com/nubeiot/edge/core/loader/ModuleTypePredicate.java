package com.nubeiot.edge.core.loader;

import java.util.List;
import java.util.function.Predicate;

import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

//TODO add more type
interface ModuleTypePredicate extends Predicate<String> {

    List<String> getSearchPattern();

    Predicate<String> getRule();

    @RequiredArgsConstructor
    @Getter
    class JavaPredicate implements ModuleTypePredicate {

        @NonNull
        private final List<String> searchPattern;

        @Override
        public boolean test(String test) {
            if (Strings.isBlank(test)) {
                return false;
            }
            return searchPattern.isEmpty() || this.searchPattern.parallelStream().anyMatch(test::startsWith);
        }

        @Override
        public Predicate<String> getRule() {
            return this;
        }

    }


    @RequiredArgsConstructor
    @Getter
    class JavascriptPredicate implements ModuleTypePredicate {

        @NonNull
        private final List<String> searchPattern;

        @Override
        public boolean test(String s) {
            return true;
        }

        @Override
        public Predicate<String> getRule() {
            return this;
        }

    }

}
