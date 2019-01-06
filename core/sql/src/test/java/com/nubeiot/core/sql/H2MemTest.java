package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jooq.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.pojos.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2MemTest extends BaseSqlTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    @NonNull String getJdbcUrl() {
        return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
    }

    @Test
    public void test_init_no_data(TestContext context) throws InterruptedException {
        MockManyEntityHandler entityHandler = startSQL(ManySchema.CATALOG, MockManyNoData.class, context);
        Async async = context.async(2);
        entityHandler.getQueryExecutor()
                     .executeAny(dsl -> dsl.fetch(ManySchema.TBL_SAMPLE_00))
                     .subscribe(rs -> assertSize(context, async, 0, rs));
        entityHandler.getQueryExecutor()
                     .executeAny(dsl -> dsl.fetch(ManySchema.TBL_SAMPLE_01))
                     .subscribe(rs -> assertSize(context, async, 0, rs));
        async.awaitSuccess();
    }

    @Test
    public void test_init_with_data(TestContext context) throws InterruptedException {
        JsonObject e0 = JsonPojo.from(new TblSample_00().setId(1)
                                                        .setFBool(true)
                                                        .setFStr("hello")
                                                        .setFValue(new JsonObject().put("key", "english"))).toJson();
        JsonObject e1 = JsonPojo.from(new TblSample_01().setId(1)
                                                        .setFBool(false)
                                                        .setFStr("hola")
                                                        .setFValue(new JsonObject().put("key", "spanish"))).toJson();
        List<String> excludes = Collections.singletonList(ManySchema.TBL_SAMPLE_00.F_DATE.getName().toLowerCase());
        MockManyEntityHandler entityHandler = startSQL(ManySchema.CATALOG, MockManyEntityHandler.class, context);
        Async async = context.async(4);
        fetch(context, async, entityHandler.getQueryExecutor(), ManySchema.TBL_SAMPLE_00, e0, excludes);
        fetch(context, async, entityHandler.getQueryExecutor(), ManySchema.TBL_SAMPLE_01, e1, excludes);
        async.awaitSuccess();
    }

    private void fetch(TestContext context, Async async, JDBCRXGenericQueryExecutor queryExecutor,
                       Table<? extends VertxPojo> table, JsonObject expected, List<String> excludes) {
        queryExecutor.executeAny(dsl -> dsl.fetch(table)).subscribe(rs -> {
            assertSize(context, async, 1, rs);
            JsonObject record = JsonPojo.from(rs.get(0)).toJson();
            excludes.forEach(c -> {
                context.assertNotNull(record.getValue(c));
                record.remove(c);
            });
            assertData(async, context, expected, record);
        });
    }

    @Test
    public void test_init_complex_model(TestContext context) throws InterruptedException {
        startSQL(OneSchema.CATALOG, MockOneEntityHandler.class, context);
    }

}
