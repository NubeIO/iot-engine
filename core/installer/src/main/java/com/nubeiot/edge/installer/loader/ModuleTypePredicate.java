package com.nubeiot.edge.installer.loader;

import java.util.List;
import java.util.function.Predicate;

import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

interface ModuleTypePredicate extends Predicate<String> {

    List<String> getSearchPattern();

    default Predicate<String> getRule() {
        return this;
    }

    @RequiredArgsConstructor
    @Getter
    abstract class AbstractModuleTypePredicate implements ModuleTypePredicate {

        @NonNull
        protected final List<String> searchPattern;

    }


    class JavaPredicate extends AbstractModuleTypePredicate {

        JavaPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String test) {
            if (Strings.isBlank(test)) {
                return false;
            }
            return searchPattern.isEmpty() || this.searchPattern.parallelStream().anyMatch(test::startsWith);
        }

    }


    class JavascriptPredicate extends AbstractModuleTypePredicate {

        JavascriptPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String s) {
            return true;
        }

    }


    class GroovyPredicate extends AbstractModuleTypePredicate {

        GroovyPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String test) {
            return true;
        }

    }


    class ScalaPredicate extends AbstractModuleTypePredicate {

        ScalaPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String test) {
            return true;
        }

    }


    class KotlinPredicate extends AbstractModuleTypePredicate {

        KotlinPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String test) {
            return true;
        }

    }


    class RubyPredicate extends AbstractModuleTypePredicate {

        RubyPredicate(@NonNull List<String> searchPattern) {
            super(searchPattern);
        }

        @Override
        public boolean test(String test) {
            return true;
        }

    }

}
