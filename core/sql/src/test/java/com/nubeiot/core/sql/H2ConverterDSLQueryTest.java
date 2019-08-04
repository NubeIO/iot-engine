package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.Tables;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.records.TblSample_01Record;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2ConverterDSLQueryTest extends BaseSqlDslConverterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Override
    protected Class<? extends MockManyEntityHandler> handler() { return MockManyNoData.class; }

    @Override
    @NonNull
    public String getJdbcUrl() {
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
