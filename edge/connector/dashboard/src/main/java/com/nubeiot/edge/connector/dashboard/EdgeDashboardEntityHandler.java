package com.nubeiot.edge.connector.dashboard;

import static com.nubeiot.edge.connector.dashboard.model.tables.TblDashboardConnection.TBL_DASHBOARD_CONNECTION;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.connector.dashboard.model.tables.daos.TblDashboardConnectionDao;
import com.nubeiot.edge.connector.dashboard.model.tables.interfaces.ITblDashboardConnection;
import com.nubeiot.edge.connector.dashboard.model.tables.pojos.TblDashboardConnection;

public class EdgeDashboardEntityHandler extends AbstractEntityHandler {

    private final TblDashboardConnectionDao connectionDao;

    protected EdgeDashboardEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        connectionDao = dao(TblDashboardConnectionDao.class);
    }

    @Override
    public boolean isNew() {
        return isNew(TBL_DASHBOARD_CONNECTION);
    }

    @Override
    public Single<EventMessage> initData() {
        EdgeDashboardConnectionConfig config = IConfig.from(
            sharedData(EdgeDashboardServerVerticle.SHARED_EDGE_DASHBOARD_CONNECTION_CONFIG),
            EdgeDashboardConnectionConfig.class);
        ITblDashboardConnection dashboardConnection = new TblDashboardConnection().fromJson(config.toJson());
        OffsetDateTime now = DateTimes.now();
        return connectionDao.insert((TblDashboardConnection) dashboardConnection.setCreatedAt(now).setModifiedAt(now))
                            .map(i -> EventMessage.success(EventAction.INIT, dashboardConnection.toJson()));
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.success(EventAction.MIGRATE, new JsonObject()));
    }

    Single<List<TblDashboardConnection>> findDashboardConnectionRecords() {
        return connectionDao.findAll();
    }

    Single<Optional<TblDashboardConnection>> findDashboardConnectionRecord(String value) {
        return connectionDao.findOneById(Byte.parseByte(value));
    }

    Single<Integer> updateRecord(JsonObject record) {
        TblDashboardConnection dashboardConnection = (TblDashboardConnection) new TblDashboardConnection().fromJson(
            record);
        return connectionDao.update(dashboardConnection);
    }

}
