package com.nubeiot.core.sql;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import lombok.NonNull;

abstract class BaseSqlConverterTest extends BaseSqlTest {

    protected MockManyEntityHandler entityHandler;

    protected abstract Class<? extends MockManyEntityHandler> handler();

    protected abstract void initData(TestContext context);

    @Override
    protected void setup(TestContext context) {
        this.entityHandler = startSQL(context, SchemaTest.ManySchema.CATALOG, handler());
        this.initData(context);
    }

    <T> void queryAndAssert(TestContext context, Async async, int id, @NonNull String field, @NonNull T data) {
        this.entityHandler.getSample01Dao()
                          .findOneById(id)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), field, data));
    }

}
