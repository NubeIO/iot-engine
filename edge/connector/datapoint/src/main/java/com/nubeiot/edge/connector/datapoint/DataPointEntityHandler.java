package com.nubeiot.edge.connector.datapoint;

import java.util.List;
import java.util.stream.Collectors;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.unit.DataType;

import lombok.NonNull;

class DataPointEntityHandler extends EntityHandler {

    public DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.POINT);
    }

    @Override
    public Single<EventMessage> initData() {
        return setupData(EventAction.INIT);
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.initial(EventAction.MIGRATE));
    }

    Single<EventMessage> setupData(EventAction action) {
        List<MeasureUnit> measureUnits = ReflectionField.streamConstants(DataType.class, DataType.class)
                                                        .map(dt -> new MeasureUnit(dt.toJson()))
                                                        .collect(Collectors.toList());
        return getDao(MeasureUnitDao.class).insert(measureUnits)
                                           .map(r -> EventMessage.success(action, new JsonObject().put("records", r)));
    }

}
