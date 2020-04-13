package io.github.zero.jooq.rql.query;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JooqFetchExistQuery extends AbstractJooqConditionQuery<Boolean> {

    @Override
    public Boolean execute(@NonNull Condition condition) {
        return dsl().fetchExists(dsl().select(DSL.asterisk()).from(table()).where(condition));
    }

}
