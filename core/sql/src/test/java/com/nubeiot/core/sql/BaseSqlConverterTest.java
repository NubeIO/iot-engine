package com.nubeiot.core.sql;

import org.junit.After;
import org.junit.Before;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import lombok.NonNull;

abstract class BaseSqlConverterTest extends BaseSqlTest {

    protected MockManyEntityHandler entityHandler;

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.entityHandler = startSQL(context, ManySchema.CATALOG, handler());
        this.initData(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    protected abstract Class<? extends MockManyEntityHandler> handler();

    protected abstract void initData(TestContext context);

    protected <T> void queryAndAssert(TestContext context, Async async, int id, @NonNull String field,
                                      @NonNull T data) {
        this.entityHandler.getSample01Dao()
                          .findOneById(id)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), field, data));
    }

}
