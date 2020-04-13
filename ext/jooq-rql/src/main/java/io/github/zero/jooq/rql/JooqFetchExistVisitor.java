package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.TableLike;
import org.jooq.impl.DSL;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class JooqFetchExistVisitor extends AbstractJooqVisitor<Boolean> implements JooqRqlFacadeVisitor<Boolean> {

    @NonNull
    private final TableLike table;

    @Override
    protected Boolean build(@NonNull Node node, @NonNull DSLContext dsl) {
        return dsl.fetchExists(dsl.select(DSL.asterisk()).from(table).where(conditionVisitor().build(node, dsl)));
    }

}
