package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.SQLStateClass;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.DatabaseException;
import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;
import com.opentable.db.postgres.embedded.EmbeddedPostgres.Builder;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class PostgresErrorTest extends BaseSqlTest {

    @Rule
    public SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance().customize(this::pgBuilder);
    private DSLContext dsl;
    private MockManyEntityHandler entityHandler;

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    private void pgBuilder(Builder builder) {
        builder.setCleanDataDirectory(true).setPort(10002);
    }

    @Before
    public void before(TestContext context) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        super.before(context);
        this.entityHandler = startSQL(context, ManySchema.CATALOG, MockManyNoData.class);
        this.dsl = entityHandler.getJooqConfig().dsl();
        TblSample_01 pojo = new TblSample_01().setId(3)
                                              .setFDate_1(LocalDate.of(2019, 2, 17))
                                              .setFTimestamp(LocalDateTime.of(2019, 2, 17, 23, 59, 59))
                                              .setFTimestampz(LocalDateTime.of(2019, 2, 17, 23, 59, 59))
                                              .setFTime(LocalTime.of(23, 59, 59))
                                              .setFDuration(Duration.ofHours(50).plusMinutes(30).plusSeconds(20))
                                              .setFPeriod(Period.ofYears(2).plusMonths(3).plusDays(33));
        Single<Integer> insert1 = this.entityHandler.getSample01Dao().insert(pojo);

        Async async = context.async();
        insert1.subscribe(result -> {
            System.out.println("Insert successfully!");
            TestHelper.testComplete(async);
        }, error -> {
            System.out.println("Insert failed!");
            context.fail("Error");
            TestHelper.testComplete(async);
        });
        async.awaitSuccess();
    }

    @Override
    @NonNull String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_insert_error(TestContext context) {
        Async async = context.async();
        TblSample_01 pojo = new TblSample_01().setId(3)
                                              .setFDate_1(LocalDate.of(2019, 2, 17))
                                              .setFTimestamp(LocalDateTime.of(2019, 2, 17, 0, 0, 1))
                                              .setFTimestampz(LocalDateTime.of(2019, 2, 17, 0, 0, 1))
                                              .setFTime(LocalTime.of(0, 0, 01))
                                              .setFDuration(Duration.ofHours(50).plusMinutes(30).plusSeconds(20))
                                              .setFPeriod(Period.ofYears(2).plusMonths(3).plusDays(33));
        this.entityHandler.getSample01Dao().insert(pojo).subscribe(result -> {
        }, error -> {
            System.out.println(error.getMessage());
            context.assertTrue(error.getMessage().contains(SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.name()));
            context.assertTrue(error instanceof DatabaseException);
            TestHelper.testComplete(async);
        });
        async.awaitSuccess();
    }

}
