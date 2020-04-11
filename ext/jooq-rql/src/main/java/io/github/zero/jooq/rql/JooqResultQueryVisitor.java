package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * Represents for Jooq result query visitor.
 *
 * @param <R> Type of {@code Record}
 * @param <T> Type of {@code Table}
 * @see Record
 * @see Table
 * @see SelectConditionStep
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
@SuperBuilder
public class JooqResultQueryVisitor<R extends Record, T extends Table<R>>
    extends AbstractJooqVisitor<R, T, SelectConditionStep<R>> {

    @Override
    public SelectConditionStep<R> visit(AndNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @Override
    public SelectConditionStep<R> visit(OrNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @Override
    public SelectConditionStep<R> visit(ComparisonNode node, DSLContext dsl) {
        return build(node, dsl);
    }

    @NonNull
    protected SelectConditionStep<? extends Record> firstClause(@NonNull DSLContext dsl) {
        return dsl.select(queryContext().fieldSelector().get()).from(table()).where(DSL.trueCondition());
    }

    @NonNull
    protected SelectConditionStep<R> build(@NonNull Node node, @NonNull DSLContext dsl) {
        return (SelectConditionStep<R>) criteriaBuilderFactory().create(node)
                                                                .build(table(), queryContext(), firstClause(dsl));
    }

}
