package com.nubeiot.core.sql;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.DatabaseException;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.daos.TblSample_00Dao;
import com.nubeiot.core.sql.mock.manyschema.mock0.tables.pojos.TblSample_00;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.daos.TblSample_01Dao;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

import lombok.Getter;

@Getter
public class MockManyEntityHandler extends EntityHandler {

    private final TblSample_00Dao sample00Dao;
    private final TblSample_01Dao sample01Dao;

    public MockManyEntityHandler(Configuration jooqConfig, Vertx vertx) {
        super(jooqConfig, vertx);
        this.sample00Dao = new TblSample_00Dao(jooqConfig, getVertx());
        this.sample01Dao = new TblSample_01Dao(jooqConfig, getVertx());
    }

    @Override
    public boolean isNew() {
        return isNew(com.nubeiot.core.sql.mock.manyschema.mock0.tables.TblSample_00.TBL_SAMPLE_00);
    }

    @Override
    public Single<EventMessage> initData() {
        Single<Integer> insert00 = sample00Dao.insert(
            new TblSample_00().setFBool(true).setFStr("hello").setFValue(new JsonObject().put("key", "english")));
        Single<Integer> insert01 = sample01Dao.insert(
            new TblSample_01().setFBool(false).setFStr("hola").setFValue(new JsonObject().put("key", "spanish")));
        return Single.zip(insert00, insert01, (r1, r2) -> r1 + r2)
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.success(EventAction.MIGRATE, new JsonObject().put("records", "No migrate")));
    }

    public static class MockManyNoData extends MockManyEntityHandler {

        public MockManyNoData(Configuration jooqConfig, Vertx vertx) {
            super(jooqConfig, vertx);
        }

        @Override
        public Single<EventMessage> initData() {
            return Single.just(EventMessage.success(EventAction.INIT, new JsonObject().put("records", "No data")));
        }

    }


    public static class MockManyErrorData extends MockManyEntityHandler {

        public MockManyErrorData(Configuration jooqConfig, Vertx vertx) {
            super(jooqConfig, vertx);
        }

        @Override
        public Single<EventMessage> initData() {
            return Single.just(EventMessage.error(EventAction.INIT, new DatabaseException("Init error")));
        }

        @Override
        public Single<EventMessage> migrate() {
            return Single.just(EventMessage.error(EventAction.MIGRATE, new DatabaseException("Migrate error")));
        }

    }

}
