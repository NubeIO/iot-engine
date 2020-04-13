package io.github.zero.jooq.rql.query;

import org.jooq.Condition;
import org.jooq.TableLike;

import io.github.zero.jooq.rql.JooqRqlQuery;

import lombok.NonNull;

public interface JooqConditionQuery<R> extends JooqRqlQuery<R, Condition> {

    @NonNull TableLike table();

    @NonNull R execute(@NonNull Condition condition);

}
