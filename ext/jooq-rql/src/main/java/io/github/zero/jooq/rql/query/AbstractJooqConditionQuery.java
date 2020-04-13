package io.github.zero.jooq.rql.query;

import org.jooq.Condition;
import org.jooq.TableLike;

import io.github.zero.jooq.rql.JooqRqlVisitor;
import io.github.zero.jooq.rql.visitor.JooqRqlConditionVisitor;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public abstract class AbstractJooqConditionQuery<R> extends AbstractJooqQuery<R, Condition>
    implements JooqConditionQuery<R> {

    @NonNull
    private final TableLike table;

    @Override
    public @NonNull JooqRqlVisitor<Condition> visitor() {
        return JooqRqlConditionVisitor.builder()
                                      .table(table())
                                      .queryContext(queryContext())
                                      .criteriaBuilderFactory(criteriaBuilderFactory())
                                      .build();
    }

    public @NonNull R execute(@NonNull String query) {
        return execute(parser().parse(query, dsl(), visitor()));
    }

}
