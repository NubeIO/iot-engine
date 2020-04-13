package io.github.zero.jooq.rql.visitor;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.TableLike;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * Represents for Jooq condition visitor.
 *
 * @see Condition
 * @since 1.0.0
 */
@Getter
@SuperBuilder
public final class JooqRqlConditionVisitor extends AbstractJooqVisitor<Condition> {

    @NonNull
    private final TableLike table;

    @NonNull
    protected Condition build(@NonNull Node node, @NonNull DSLContext dsl) {
        return criteriaBuilderFactory().create(node).build(table, queryContext(), criteriaBuilderFactory());
    }

}
