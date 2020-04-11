package io.github.zero.jooq.rql.criteria;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;

import io.github.zero.jooq.rql.QueryContext;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.NonNull;

/**
 * The interface Criteria builder.
 *
 * @param <T> Type of {@code Node}
 * @since 1.0.0
 */
public interface CriteriaBuilder<T extends Node> {

    /**
     * Defines Node.
     *
     * @return the node
     * @since 1.0.0
     */
    @NonNull T node();

    /**
     * Build condition.
     *
     * @param table        the table
     * @param queryContext the query context
     * @param select       the select
     * @return the condition
     * @see Condition
     * @since 1.0.0
     */
    @NonNull Condition build(@NonNull Table table, @NonNull QueryContext queryContext,
                             @NonNull SelectConditionStep<? extends Record> select);

}
