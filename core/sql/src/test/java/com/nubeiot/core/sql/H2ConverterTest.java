package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2ConverterTest extends BaseSqlTest {

    private DSLContext dsl;
    private MockManyEntityHandler entityHandler;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        super.before(context);
        this.entityHandler = startSQL(context, ManySchema.CATALOG, MockManyNoData.class);
        this.dsl = entityHandler.getJooqConfig().dsl();
        //id = 1, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 23:59:59, f_timestampz = 2019-02-17 23:59:59
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        this.dsl.execute(
            "INSERT INTO tbl_sample_01 (id, f_date_1, f_timestamp, f_timestampz, f_time, f_duration, " + "f_period) " +
            "VALUES (1, PARSEDATETIME('2019-02-17', 'yyyy-MM-dd'), PARSEDATETIME('2019-02-17" +
            " 23:59:59', 'yyyy-MM-dd HH:mm:ss'), PARSEDATETIME('2019-02-17 23:59:59', 'yyyy-MM-dd " +
            "HH:mm:ss'), PARSEDATETIME('23:59:59', 'HH:mm:ss'), 'PT50H30M20S', 'P2Y3M4W5D');");

        //id = 2, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 00:00:01, f_timestampz = 2019-02-17 00:00:01
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        this.dsl.execute("INSERT INTO tbl_sample_01 (id, f_date_1, f_timestamp, f_timestampz, f_time, f_duration, " +
                         "f_period) VALUES (2, PARSEDATETIME('2019-02-17', 'yyyy-MM-dd'), PARSEDATETIME('2019-02-17" +
                         " 00:00:01', 'yyyy-MM-dd HH:mm:ss'), PARSEDATETIME('2019-02-17 00:00:01', 'yyyy-MM-dd " +
                         "HH:mm:ss'), PARSEDATETIME('00:00:01', 'HH:mm:ss'), 'PT50H30M20S', 'P2Y3M4W5D');");
        this.entityHandler.getSample01Dao()
                          .insert(new TblSample_01().setFBool(false)
                                                    .setFStr("hola")
                                                    .setFValue(new JsonObject().put("key", "spanish")));
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
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
