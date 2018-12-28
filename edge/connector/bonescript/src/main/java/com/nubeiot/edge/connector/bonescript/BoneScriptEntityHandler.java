package com.nubeiot.edge.connector.bonescript;

import org.jooq.Configuration;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.connector.bonescript.model.Tables;
import com.nubeiot.edge.connector.bonescript.model.tables.TblDitto;
import com.nubeiot.edge.connector.bonescript.model.tables.daos.TblDittoDao;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Getter;

@Getter
public class BoneScriptEntityHandler extends EntityHandler {

    private static final Logger logger = LoggerFactory.getLogger(BoneScriptEntityHandler.class);

    protected BoneScriptEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(TblDitto.TBL_DITTO);
    }

    @Override
    public Single<EventMessage> initData() {
        return this.bootstrap(EventAction.INIT);
    }

    @Override
    public Single<EventMessage> migrate() {
        return bootstrap(EventAction.MIGRATE);
    }

    Single<Boolean> isFreshInstall() {
        return this.queryExecutor.executeAny(context -> context.fetchCount(Tables.TBL_DITTO)).map(count -> count == 0);
    }

    private Single<EventMessage> bootstrap(EventAction action) {
        TblDittoDao tblDittoDao = new TblDittoDao(jooqConfig, getVertx());
        return isFreshInstall().flatMap(isFreshInstall -> {
            if (isFreshInstall) {
                return new Init(tblDittoDao).get();
            }
            return Single.just(new JsonObject());
        }).map(r -> EventMessage.success(action, r));
    }

}
