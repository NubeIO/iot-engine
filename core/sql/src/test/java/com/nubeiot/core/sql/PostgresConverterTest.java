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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.opentable.db.postgres.embedded.EmbeddedPostgres.Builder;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class PostgresConverterTest extends BaseSqlTest {

    private MockManyNoData entityHandler;
    private DSLContext dsl;

    @Rule
    public SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance().customize(this::pgBuilder);

    private void pgBuilder(Builder builder) {
        builder.setCleanDataDirectory(true).setPort(10001);
    }

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        super.before(context);
        this.entityHandler = startSQL(context, ManySchema.CATALOG, MockManyNoData.class);

        this.dsl = this.entityHandler.getJooqConfig().dsl();
        //id = 1, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 23:59:59, f_timestampz = 2019-02-17 23:59:59
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        dsl.execute(
            "INSERT INTO \"mock1\".\"TBL_SAMPLE_01\" (\"ID\", \"F_DATE_1\", \"F_TIMESTAMP\", \"F_TIMESTAMPZ\", " +
            "\"F_TIME\", \"F_DURATION\", \"F_PERIOD\") VALUES (1, '2019-02-17', '2019-02-17 23:59:59', '2019-02-17 " +
            "23:59:59', '23:59:59', 'PT50H30M20S', 'P2Y3M4W5D');");

        //id = 2, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 00:00:01, f_timestampz = 2019-02-17 00:00:01
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        dsl.execute(
            "INSERT INTO \"mock1\".\"TBL_SAMPLE_01\" (\"ID\", \"F_DATE_1\", \"F_TIMESTAMP\", \"F_TIMESTAMPZ\", " +
            "\"F_TIME\", \"F_DURATION\", \"F_PERIOD\") VALUES (2, '2019-02-17', '2019-02-17 00:00:01', '2019-02-17 " +
            "00:00:01', '00:00:01', 'PT50H30M20S', 'P2Y3M4W5D');");
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    @NonNull String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

    @Test
    public void test_date_converter(TestContext context) {
        Async async = context.async(2);
        this.entityHandler.getSample01Dao()
                          .findOneById(1)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fDate_1",
                                                           LocalDate.of(2019, 2, 17), LocalDate.class));

        this.entityHandler.getSample01Dao()
                          .findOneById(2)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fDate_1",
                                                           LocalDate.of(2019, 2, 17), LocalDate.class));
        async.awaitSuccess();
    }

    @Test
    public void test_timestamp_converter(TestContext context) {
        Async async = context.async(2);
        this.entityHandler.getSample01Dao()
                          .findOneById(1)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fTimestamp",
                                                           LocalDateTime.of(2019, 2, 17, 23, 59, 59),
                                                           LocalDateTime.class));

        this.entityHandler.getSample01Dao()
                          .findOneById(2)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fTimestamp",
                                                           LocalDateTime.of(2019, 2, 17, 0, 0, 1),
                                                           LocalDateTime.class));
        async.awaitSuccess();
    }

    @Test
    public void test_time_converter(TestContext context) {
        Async async = context.async(2);
        this.entityHandler.getSample01Dao()
                          .findOneById(1)
                          .subscribe(result -> this.assertValue(context, async, result.orElse(null), "fTime",
                                                                LocalTime.of(23, 59, 59), LocalTime.class));

        this.entityHandler.getSample01Dao()
                          .findOneById(2)
                          .subscribe(result -> this.assertValue(context, async, result.orElse(null), "fTime",
                                                                LocalTime.of(0, 0, 1), LocalTime.class));
        async.awaitSuccess();
    }

    @Test
    public void test_duration_converter(TestContext context) {
        Async async = context.async(1);
        ////f_duration = PT50H30M20S
        this.entityHandler.getSample01Dao()
                          .findOneById(1)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fDuration",
                                                           Duration.ofHours(50).plusMinutes(30).plusSeconds(20),
                                                           Duration.class));
        async.awaitSuccess();
    }

    @Test
    public void test_period_converter(TestContext context) {
        Async async = context.async(1);
        //f_period= P2Y3M4W5D
        this.entityHandler.getSample01Dao()
                          .findOneById(1)
                          .subscribe(result -> assertValue(context, async, result.orElse(null), "fPeriod",
                                                           Period.ofYears(2).plusMonths(3).plusDays(33), Period.class));
        async.awaitSuccess();
    }

}
