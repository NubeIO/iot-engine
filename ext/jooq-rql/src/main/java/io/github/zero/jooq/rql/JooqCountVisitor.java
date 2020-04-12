package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.Table;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JooqCountVisitor extends AbstractJooqVisitor<Integer> implements JooqRqlDelegateVisitor<Integer> {

    @NonNull
    private final Table table;

    @Override
    protected Integer build(@NonNull Node node, @NonNull DSLContext dsl) {
        return dsl.fetchCount(table, createConditionVisitor(table).build(node, dsl));
    }

}
