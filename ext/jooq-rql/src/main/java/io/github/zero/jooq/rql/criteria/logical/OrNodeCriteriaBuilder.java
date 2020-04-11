package io.github.zero.jooq.rql.criteria.logical;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;

import io.github.zero.jooq.rql.QueryContext;
import io.github.zero.jooq.rql.criteria.AbstractCriteriaBuilder;

import cz.jirutka.rsql.parser.ast.OrNode;
import lombok.NonNull;

public class OrNodeCriteriaBuilder extends AbstractCriteriaBuilder<OrNode> implements LogicalCriteriaBuilder<OrNode> {

    public OrNodeCriteriaBuilder(@NonNull OrNode node) {
        super(node);
    }

    @Override
    public @NonNull Condition build(@NonNull Table table, @NonNull QueryContext queryContext,
                                    @NonNull SelectConditionStep<? extends Record> select) {
        return null;
    }

}
