package io.github.zero.jooq.rql;

import java.util.Optional;

import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero.jooq.rql.criteria.CriteriaBuilderFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Represents for Abstract jooq visitor.
 *
 * @param <R> Type of {@code Record}
 * @param <T> Type of {@code Table}
 * @param <O> Type of {@code Result}
 * @see Table
 * @see Record
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@Accessors(fluent = true)
public abstract class AbstractJooqVisitor<R extends Record, T extends Table<R>, O> implements JooqRqlVisitor<R, T, O> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @NonNull
    private final T table;
    private final QueryContext queryContext;
    private final CriteriaBuilderFactory criteriaBuilderFactory;

    @Override
    public @NonNull QueryContext queryContext() {
        return Optional.ofNullable(queryContext).orElseGet(JooqRqlVisitor.super::queryContext);
    }

    @Override
    public @NonNull CriteriaBuilderFactory criteriaBuilderFactory() {
        return Optional.ofNullable(criteriaBuilderFactory).orElseGet(JooqRqlVisitor.super::criteriaBuilderFactory);
    }

}
