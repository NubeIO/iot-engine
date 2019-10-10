package com.nubeiot.core.sql;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.pojos.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;
import com.nubeiot.core.sql.pojos.JsonPojo;

@RunWith(VertxUnitRunner.class)
public class H2MemTest extends BaseSqlTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Test
    public void test_init_no_data(TestContext context) {
        MockManyEntityHandler entityHandler = startSQL(context, SchemaTest.ManySchema.CATALOG, MockManyNoData.class);
        Async async = context.async(2);
        entityHandler.genericQuery().executeAny(dsl -> dsl.fetch(SchemaTest.ManySchema.TBL_SAMPLE_00))
                     .subscribe(rs -> assertSize(context, async, 0, rs));
        entityHandler.genericQuery().executeAny(dsl -> dsl.fetch(SchemaTest.ManySchema.TBL_SAMPLE_01))
                     .subscribe(rs -> assertSize(context, async, 0, rs));
    }

    @Test
    public void test_init_with_data(TestContext context) {
        JsonObject e0 = JsonPojo.from(new TblSample_00().setId(1)
                                                        .setFBool(true)
                                                        .setFStr("hello")
                                                        .setFValue(new JsonObject().put("key", "english"))).toJson();
        JsonObject e1 = JsonPojo.from(new TblSample_01().setId(1)
                                                        .setFBool(false)
                                                        .setFStr("hola")
                                                        .setFValue(new JsonObject().put("key", "spanish"))).toJson();

        MockManyEntityHandler entityHandler = startSQL(context, SchemaTest.ManySchema.CATALOG,
                                                       MockManyEntityHandler.class);
        Async async = context.async(4);
        fetch(context, async, entityHandler.genericQuery(), SchemaTest.ManySchema.TBL_SAMPLE_00, e0);
        fetch(context, async, entityHandler.genericQuery(), SchemaTest.ManySchema.TBL_SAMPLE_01, e1);
    }

    private void fetch(TestContext context, Async async, JDBCRXGenericQueryExecutor queryExecutor,
                       Table<? extends VertxPojo> table, JsonObject expected) {
        queryExecutor.executeAny(dsl -> dsl.fetch(table)).subscribe(rs -> {
            assertSize(context, async, 1, rs);
            JsonObject record = JsonPojo.from(rs.get(0)).toJson();
            JsonHelper.assertJson(context, async, expected, record);
        });
    }

    @Test
    public void test_init_complex_model(TestContext context) {
        startSQL(context, SchemaTest.OneSchema.CATALOG, MockOneEntityHandler.class);
    }

    private void assertSize(TestContext context, Async async, int expected, Result<? extends Record> rs) {
        try {
            context.assertEquals(expected, rs.size());
        } catch (AssertionError e) {
            context.fail(e);
        } finally {
            TestHelper.testComplete(async);
        }
    }

}
