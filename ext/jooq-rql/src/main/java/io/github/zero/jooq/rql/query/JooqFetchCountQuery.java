package io.github.zero.jooq.rql.query;

import org.jooq.Condition;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JooqFetchCountQuery extends AbstractJooqConditionQuery<Integer> {

    @Override
    public Integer execute(@NonNull Condition condition) {
        return dsl().selectCount().from(table()).where(condition).fetch().get(0).value1();
    }

}