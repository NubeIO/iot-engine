package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.Tables;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.records.TblSample_01Record;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2ConverterDSLQueryTest extends BaseSqlTest {

    private DSLContext dsl;

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
        EntityHandler entityHandler = startSQL(context, ManySchema.CATALOG, MockManyNoData.class);
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
    public void test_date_converter() {
        TblSample_01Record tblSample_01Record1 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(1))
                                                         .fetchAny();
        Assert.assertEquals(tblSample_01Record1.getFDate_1(), LocalDate.of(2019, 2, 17));

        TblSample_01Record tblSample_01Record2 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(2))
                                                         .fetchAny();
        Assert.assertEquals(tblSample_01Record2.getFDate_1(), LocalDate.of(2019, 2, 17));
    }

    @Test
    public void test_timestamp_converter() {
        TblSample_01Record tblSample_01Record1 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(1))
                                                         .fetchAny();
        System.out.println(tblSample_01Record1.getFTimestamp());
        Assert.assertEquals(tblSample_01Record1.getFTimestamp(),
                            OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC));
        TblSample_01Record tblSample_01Record2 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(2))
                                                         .fetchAny();
        Assert.assertEquals(tblSample_01Record2.getFTimestamp(),
                            OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC));
    }

    @Test
    public void test_time_converter() {
        TblSample_01Record tblSample_01Record1 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(1))
                                                         .fetchAny();
        Assert.assertEquals(tblSample_01Record1.getFTime(), LocalTime.of(23, 59, 59));

        TblSample_01Record tblSample_01Record2 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(2))
                                                         .fetchAny();
        Assert.assertEquals(tblSample_01Record2.getFTime(), LocalTime.of(0, 0, 1));
    }

    @Test
    public void test_duration_converter() {
        TblSample_01Record tblSample_01Record1 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(1))
                                                         .fetchAny();
        //f_duration = PT50H30M20S
        Assert.assertEquals(tblSample_01Record1.getFDuration(), Duration.ofHours(50).plusMinutes(30).plusSeconds(20));
    }

    @Test
    public void test_period_converter() {
        TblSample_01Record tblSample_01Record1 = this.dsl.selectFrom(Tables.TBL_SAMPLE_01)
                                                         .where(Tables.TBL_SAMPLE_01.ID.eq(1))
                                                         .fetchAny();
        //f_period= P2Y3M4W5D
        Assert.assertEquals(tblSample_01Record1.getFPeriod(), Period.ofYears(2).plusMonths(3).plusDays(33));
    }

}
