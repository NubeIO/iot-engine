package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.Table;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JooqFetchExistVisitor extends AbstractJooqVisitor<Boolean>
    implements JooqRqlDelegateVisitor<Boolean> {

    @NonNull
    private final Table table;

    @Override
    protected Boolean build(@NonNull Node node, @NonNull DSLContext dsl) {
        return dsl.fetchExists(table, createConditionVisitor(table).build(node, dsl));
    }

}
