package com.nubeiot.edge.module.datapoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Table;

import io.github.zero.utils.Strings;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.SchemaInitializer;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.trigger.DataPointTriggerInitializer;
import com.nubeiot.iotdata.edge.model.Tables;

import lombok.NonNull;

final class DataPointInitializer implements SchemaInitializer {

    @Override
    public int createTriggers(@NonNull DSLContext dsl) {
        return new DataPointTriggerInitializer().execute(dsl);
    }

    @Override
    public int doMisc(@NonNull DSLContext dsl) {
        Map<Table, Field<UUID>> map = new HashMap<>();
        map.put(Tables.EDGE, Tables.EDGE.ID);
        map.put(Tables.DEVICE, Tables.DEVICE.ID);
        map.put(Tables.NETWORK, Tables.NETWORK.ID);
        map.put(Tables.POINT, Tables.POINT.ID);
        map.put(Tables.THING, Tables.THING.ID);
        final String sql = "Alter table {0} alter column {1} set default random_uuid()";
        if (dsl.configuration().family() == SQLDialect.H2) {
            return map.entrySet()
                      .stream()
                      .map(entry -> dsl.execute(
                          Strings.format(sql, entry.getKey().getName(), entry.getValue().getName())))
                      .reduce(0, Integer::sum);
        }
        return 0;
    }

    @Override
    public @NonNull Single<JsonObject> initData(@NonNull EntityHandler handler) {
        return ((DataPointEntityHandler) handler).initDataFromConfig(EventAction.INIT)
                                                 .doOnSuccess(ignore -> new DataCacheInitializer().init(handler));
    }

}
