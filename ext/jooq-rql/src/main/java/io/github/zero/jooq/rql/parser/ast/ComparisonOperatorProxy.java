package io.github.zero.jooq.rql.parser.ast;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero.jooq.rql.criteria.comparision.BetweenBuilder;
import io.github.zero.jooq.rql.criteria.comparision.ExistsBuilder;
import io.github.zero.jooq.rql.criteria.comparision.NonExistsBuilder;
import io.github.zero.jooq.rql.criteria.comparision.NullableBuilder;
import io.github.zero.utils.Reflections.ReflectionField;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.NonNull;

public interface ComparisonOperatorProxy {

    ComparisonOperatorProxy EQUAL = () -> RSQLOperators.EQUAL;
    ComparisonOperatorProxy NOT_EQUAL = () -> RSQLOperators.NOT_EQUAL;
    ComparisonOperatorProxy GREATER_THAN = () -> RSQLOperators.GREATER_THAN;
    ComparisonOperatorProxy GREATER_THAN_OR_EQUAL = () -> RSQLOperators.GREATER_THAN_OR_EQUAL;
    ComparisonOperatorProxy LESS_THAN = () -> RSQLOperators.LESS_THAN;
    ComparisonOperatorProxy LESS_THAN_OR_EQUAL = () -> RSQLOperators.LESS_THAN_OR_EQUAL;
    ComparisonOperatorProxy IN = () -> RSQLOperators.IN;
    ComparisonOperatorProxy NOT_IN = () -> RSQLOperators.NOT_IN;
    ComparisonOperatorProxy BETWEEN = () -> BetweenBuilder.OPERATOR;
    ComparisonOperatorProxy EXISTS = () -> ExistsBuilder.OPERATOR;
    ComparisonOperatorProxy NON_EXISTS = () -> NonExistsBuilder.OPERATOR;
    ComparisonOperatorProxy NULLABLE = () -> NullableBuilder.OPERATOR;

    static @NonNull ComparisonOperatorProxy asProxy(@NonNull ComparisonOperator operator) {
        return ReflectionField.streamConstants(ComparisonOperatorProxy.class)
                              .filter(proxy -> proxy.operator().equals(operator))
                              .findFirst()
                              .orElseThrow(() -> new IllegalArgumentException("Unknown operation " + operator));
    }

    static Set<ComparisonOperatorProxy> operators() {
        return ReflectionField.streamConstants(ComparisonOperatorProxy.class).collect(Collectors.toSet());
    }

    @NonNull ComparisonOperator operator();

}
