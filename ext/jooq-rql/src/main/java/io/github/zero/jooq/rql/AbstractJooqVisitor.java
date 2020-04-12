package io.github.zero.jooq.rql;

import java.util.Optional;

import org.jooq.DSLContext;

import io.github.zero.jooq.rql.criteria.CriteriaBuilderFactory;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * Represents for Abstract jooq visitor.
 *
 * @param <R> Type of {@code Result}
 * @since 1.0.0
 */
@Getter
@Accessors(fluent = true)
@SuperBuilder
public abstract class AbstractJooqVisitor<R> implements JooqRqlVisitor<R> {

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

    @Override
    public R visit(AndNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @Override
    public R visit(OrNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @Override
    public R visit(ComparisonNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @NonNull
    protected abstract R build(@NonNull Node node, @NonNull DSLContext dsl);

}
