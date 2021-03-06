package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import org.jooq.SQLDialect;
import org.jooq.exception.SQLStateClass;
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
    private MockManyEntityHandler entityHandler;

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    private void pgBuilder(Builder builder) {
        builder.setCleanDataDirectory(true).setPort(10002);
    }

    protected void setup(TestContext context) {
        this.entityHandler = startSQL(context, SchemaTest.ManySchema.CATALOG, MockManyNoData.class);
        TblSample_01 pojo = new TblSample_01().setId(3)
                                              .setFDate_1(LocalDate.of(2019, 2, 17))
                                              .setFTimestamp(
                                                  OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC))
                                              .setFTimestampz(
                                                  OffsetDateTime.of(2019, 2, 17, 23, 59, 59, 0, ZoneOffset.UTC))
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
    }

    @Override
    @NonNull
    public String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Test
    public void test_insert_error(TestContext context) {
        Async async = context.async();
        TblSample_01 pojo = new TblSample_01().setId(3)
                                              .setFDate_1(LocalDate.of(2019, 2, 17))
                                              .setFTimestamp(OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC))
                                              .setFTimestampz(
                                                  OffsetDateTime.of(2019, 2, 17, 0, 0, 1, 0, ZoneOffset.UTC))
                                              .setFTime(LocalTime.of(0, 0, 1))
                                              .setFDuration(Duration.ofHours(50).plusMinutes(30).plusSeconds(20))
                                              .setFPeriod(Period.ofYears(2).plusMonths(3).plusDays(33));
        this.entityHandler.getSample01Dao().insert(pojo).subscribe(result -> {}, error -> {
            System.out.println(error.getMessage());
            context.assertTrue(error.getMessage().contains(SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.name()));
            context.assertTrue(error instanceof DatabaseException);
            TestHelper.testComplete(async);
        });
    }

}
