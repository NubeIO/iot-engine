package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero.jooq.rql.criteria.CriteriaBuilderFactory;

import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import lombok.NonNull;

/**
 * The interface Jooq rql visitor.
 *
 * @param <R> Type of {@code Record}
 * @param <T> Type of {@code Table}
 * @param <O> Type of {@code result}
 * @see Record
 * @see Table
 * @see DSLContext
 * @see RSQLVisitor
 * @since 1.0.0
 */
public interface JooqRqlVisitor<R extends Record, T extends Table<R>, O> extends RSQLVisitor<O, DSLContext> {

    /**
     * Gets Table.
     *
     * @return the table
     * @since 1.0.0
     */
    @NonNull T table();

    /**
     * Gets Query context.
     *
     * @return the query context
     * @see QueryContext
     * @since 1.0.0
     */
    default @NonNull QueryContext queryContext() {
        return QueryContext.DEFAULT;
    }

    /**
     * Criteria builder factory criteria builder factory.
     *
     * @return the criteria builder factory
     * @see CriteriaBuilderFactory
     * @since 1.0.0
     */
    default @NonNull CriteriaBuilderFactory criteriaBuilderFactory() {
        return CriteriaBuilderFactory.DEFAULT;
    }

    /**
     * Gets logger.
     *
     * @return the logger
     * @since 1.0.0
     */
    default Logger log() {
        return LoggerFactory.getLogger(getClass());
    }

}
