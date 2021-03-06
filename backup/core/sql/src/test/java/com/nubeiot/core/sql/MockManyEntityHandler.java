package com.nubeiot.core.sql;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

import org.jooq.Catalog;
import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.DatabaseException;
import com.nubeiot.core.exceptions.InitializerError.MigrationError;
import com.nubeiot.core.sql.mock.manyschema.DefaultCatalog;
import com.nubeiot.core.sql.mock.manyschema.mock0.Tables;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.daos.TblSample_00Dao;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.pojos.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.daos.TblSample_01Dao;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class MockManyEntityHandler extends AbstractEntityHandler {

    private final TblSample_00Dao sample00Dao;
    private final TblSample_01Dao sample01Dao;

    public MockManyEntityHandler(Configuration jooqConfig, Vertx vertx) {
        super(jooqConfig, vertx);
        this.sample00Dao = dao(TblSample_00Dao.class);
        this.sample01Dao = dao(TblSample_01Dao.class);
    }

    static SchemaHandler createSchemaHandler(SchemaInitializer initializer, SchemaMigrator migrator) {
        return BaseSqlTest.createSchemaHandler(Tables.TBL_SAMPLE_00, initializer, migrator);
    }

    @Override
    public @NonNull Catalog catalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public @NonNull SchemaHandler schemaHandler() {
        return createSchemaHandler(entityHandler -> {
            Single<Integer> i00 = sample00Dao.insert(
                new TblSample_00().setFBool(true).setFStr("hello").setFValue(new JsonObject().put("key", "english")));
            Single<Integer> i01 = sample01Dao.insert(
                new TblSample_01().setFBool(false).setFStr("hola").setFValue(new JsonObject().put("key", "spanish")));
            return Single.zip(i00, i01, Integer::sum).map(r -> new JsonObject().put("records", r));
        }, SchemaMigrator.NON_MIGRATOR);
    }

    public static class MockManyNoData extends MockManyEntityHandler {

        public MockManyNoData(Configuration jooqConfig, Vertx vertx) {
            super(jooqConfig, vertx);
        }

        @Override
        public @NonNull SchemaHandler schemaHandler() {
            return createSchemaHandler(handler -> Single.just(new JsonObject().put("records", "No data")),
                                       SchemaMigrator.NON_MIGRATOR);
        }

    }


    public static class MockManyErrorData extends MockManyEntityHandler {

        public MockManyErrorData(Configuration jooqConfig, Vertx vertx) {
            super(jooqConfig, vertx);
        }

        @Override
        public @NonNull SchemaHandler schemaHandler() {
            return createSchemaHandler(handler -> Single.error(new DatabaseException("Init error")),
                                       handler -> Single.error(new MigrationError("Migrate error")));
        }

    }


    public static class MockManyInsertData extends MockManyEntityHandler {

        public MockManyInsertData(Configuration jooqConfig, Vertx vertx) {
            super(jooqConfig, vertx);
        }

        @Override
        public @NonNull SchemaHandler schemaHandler() {
            return createSchemaHandler(this::init, SchemaMigrator.NON_MIGRATOR);
        }

        private Single<JsonObject> init(@NonNull EntityHandler handler) {
            TblSample_01 pojo1 = new TblSample_01().setId(1)
                                                   .setFDate_1(LocalDate.of(2018, 1, 1))
                                                   .setFTimestamp(
                                                       OffsetDateTime.of(2018, 1, 1, 10, 15, 0, 0, ZoneOffset.UTC))
                                                   .setFTimestampz(
                                                       OffsetDateTime.of(2018, 1, 1, 10, 15, 0, 0, ZoneOffset.UTC))
                                                   .setFTime(LocalTime.of(10, 15, 0))
                                                   .setFDuration(Duration.ofHours(2).plusMinutes(2).plusSeconds(2))
                                                   .setFPeriod(Period.ofYears(1).plusMonths(2).plusDays(3));
            TblSample_01 pojo2 = new TblSample_01().setId(2)
                                                   .setFDate_1(LocalDate.of(2018, 1, 1))
                                                   .setFTimestamp(
                                                       OffsetDateTime.of(2018, 1, 1, 10, 15, 0, 0, ZoneOffset.UTC))
                                                   .setFTimestampz(
                                                       OffsetDateTime.of(2018, 1, 1, 10, 15, 0, 0, ZoneOffset.UTC))
                                                   .setFTime(LocalTime.of(10, 15, 0))
                                                   .setFDuration(Duration.ofHours(2).plusMinutes(2).plusSeconds(2))
                                                   .setFPeriod(Period.ofYears(1).plusMonths(2).plusDays(3));
            final TblSample_01Dao dao = handler.dao(TblSample_01Dao.class);
            Single<Integer> insert00 = dao.insert(pojo1);
            Single<Integer> insert01 = dao.insert(pojo2);
            return Single.zip(insert00, insert01, Integer::sum).map(r -> new JsonObject().put("records", r));
        }

    }

}
