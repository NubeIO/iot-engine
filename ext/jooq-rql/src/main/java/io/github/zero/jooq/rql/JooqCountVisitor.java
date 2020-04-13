package io.github.zero.jooq.rql;

import org.jooq.DSLContext;
import org.jooq.TableLike;

import cz.jirutka.rsql.parser.ast.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public final class JooqCountVisitor extends AbstractJooqVisitor<Integer> implements JooqRqlFacadeVisitor<Integer> {

    @NonNull
    private final TableLike table;

    @Override
    protected Integer build(@NonNull Node node, @NonNull DSLContext dsl) {
        return dsl.selectCount().from(table).where(conditionVisitor().build(node, dsl)).fetch().get(0).value1();
    }

}
