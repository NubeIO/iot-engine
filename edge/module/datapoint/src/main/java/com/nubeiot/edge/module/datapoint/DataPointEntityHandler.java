package com.nubeiot.edge.module.datapoint;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Table;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;
import com.nubeiot.iotdata.edge.model.Keys;
import com.nubeiot.iotdata.edge.model.Tables;

import lombok.NonNull;

class DataPointEntityHandler extends AbstractEntityHandler
    implements AuditDecorator, EntityConstraintHolder, DataPointIndex {

    public static final String BUILTIN_DATA = "BUILTIN_DATA";

    DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public @NonNull Class keyClass() {
        return Keys.class;
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.POINT);
    }

    @Override
    public Single<EventMessage> initData() {
        Map<Table, Field<UUID>> map = new HashMap<>();
        map.put(Tables.DEVICE, Tables.DEVICE.ID);
        map.put(Tables.EQUIPMENT, Tables.EQUIPMENT.ID);
        map.put(Tables.NETWORK, Tables.NETWORK.ID);
        map.put(Tables.POINT, Tables.POINT.ID);
        map.put(Tables.TRANSDUCER, Tables.TRANSDUCER.ID);
        createDefaultUUID(map);
        return initDataFromConfig();
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.initial(EventAction.MIGRATE));
    }

    private Single<EventMessage> initDataFromConfig() {
        Map<EntityMetadata, Integer> dep = DataPointIndex.dependencies();
        JsonObject builtinData = SharedDataDelegate.removeLocalDataValue(vertx(), getSharedKey(), BUILTIN_DATA);
        return Optional.ofNullable(builtinData)
                       .map(data -> Single.merge(index().stream()
                                                        .filter(meta -> !(meta instanceof CompositeMetadata) &&
                                                                        data.containsKey(meta.singularKeyName()))
                                                        .sorted(Comparator.comparingInt(m -> dep.getOrDefault(m, 999)))
                                                        .map(m -> insert(m, data.getValue(m.singularKeyName())))
                                                        .collect(Collectors.toList()))
                                          .buffer(5)
                                          .reduce(0, (i, r) -> i + r.stream().reduce(0, Integer::sum))
                                          .map(r -> EventMessage.success(EventAction.INIT,
                                                                         new JsonObject().put("records", r))))
                       .orElseGet(() -> Single.just(
                           EventMessage.success(EventAction.INIT, new JsonObject().put("records", 0))));
    }

    @SuppressWarnings("unchecked")
    private Single<Integer> insert(EntityMetadata metadata, @NonNull Object data) {
        final String createdBy = "SYSTEM_INITIATOR";
        Class<? extends VertxPojo> pojoClass = metadata.modelClass();
        VertxDAO dao = metadata.dao(this);
        if (parsable(pojoClass, data)) {
            return ((Single<Integer>) dao.insert(
                AuditDecorator.addCreationAudit(true, EntityHandler.parse(pojoClass, data), createdBy))).doOnSuccess(
                initLog(pojoClass));
        }
        if (data instanceof JsonArray || data instanceof Collection) {
            final Stream<Object> stream = data instanceof JsonArray
                                          ? ((JsonArray) data).stream()
                                          : ((Collection) data).stream();
            return ((Single<Integer>) dao.insert(stream.filter(o -> parsable(pojoClass, o))
                                                       .map(o -> EntityHandler.parse(pojoClass, o))
                                                       .map(pojo -> AuditDecorator.addCreationAudit(true, pojo,
                                                                                                    createdBy))
                                                       .collect(Collectors.toList()))).doOnSuccess(initLog(pojoClass));
        }
        return Single.just(0);
    }

    private boolean parsable(@NonNull Class<? extends VertxPojo> pojoClass, @NonNull Object data) {
        return data instanceof JsonObject || data instanceof Map || pojoClass.isInstance(data);
    }

    private Consumer<Integer> initLog(@NonNull Class<? extends VertxPojo> pojoClass) {
        return r -> logger.info("Inserted {} record(s) in {}", r, pojoClass.getSimpleName());
    }

}
