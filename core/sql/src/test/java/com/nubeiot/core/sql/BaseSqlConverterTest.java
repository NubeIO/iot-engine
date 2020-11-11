package com.nubeiot.core.sql;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero88.utils.Reflections.ReflectionField;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;

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

    <T> void queryAndAssert(TestContext context, Async async, int id, @NonNull String field, @NonNull T expected) {
        this.entityHandler.getSample01Dao()
                          .findOneById(id)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), field, expected));
    }

    <T> void assertValue(@NonNull TestContext context, @NonNull Async async, VertxPojo pojo, @NonNull String field,
                         @NonNull T expect) {
        try {
            context.assertNotNull(pojo);
            context.assertEquals(expect, ReflectionField.getFieldValue(pojo, pojo.getClass().getDeclaredField(field),
                                                                       expect.getClass()));
        } catch (AssertionError | NoSuchFieldException ex) {
            context.fail(ex);
        } finally {
            TestHelper.testComplete(async);
        }
    }

}
