package io.github.zero.jooq.rql.parser.ast;

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

    static @NonNull ComparisonOperatorProxy asProxy(@NonNull ComparisonOperator operator) {
        return null;
    }

    @NonNull ComparisonOperator operator();

}
