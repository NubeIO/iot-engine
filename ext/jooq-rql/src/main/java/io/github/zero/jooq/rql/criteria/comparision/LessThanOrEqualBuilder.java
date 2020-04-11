package io.github.zero.jooq.rql.criteria.comparision;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;

import io.github.zero.jooq.rql.QueryContext;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.NonNull;

public final class LessThanOrEqualBuilder extends AbstractComparisionCriteriaBuilder {

    static final ComparisonOperator OPERATOR = RSQLOperators.LESS_THAN_OR_EQUAL;

    public LessThanOrEqualBuilder(@NonNull ComparisonNode node) {
        super(node);
    }

    @Override
    public @NonNull Condition build(@NonNull Table table, @NonNull QueryContext queryContext,
                                    @NonNull SelectConditionStep<? extends Record> select) {
        return null;
    }

}
