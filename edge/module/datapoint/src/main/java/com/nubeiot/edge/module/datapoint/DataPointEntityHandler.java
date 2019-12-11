package com.nubeiot.edge.module.datapoint;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.workflow.TaskExecuter;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.task.sync.SyncServiceFactory;
import com.nubeiot.iotdata.edge.model.Keys;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class DataPointEntityHandler extends AbstractEntityHandler
    implements AuditDecorator, EntityConstraintHolder, DataPointIndex, EntitySyncHandler {

    DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public @NonNull Class keyClass() {
        return Keys.class;
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.EDGE);
    }

    @Override
    public Single<EventMessage> initData() {
        Map<Table, Field<UUID>> map = new HashMap<>();
        map.put(Tables.EDGE, Tables.EDGE.ID);
        map.put(Tables.DEVICE, Tables.DEVICE.ID);
        map.put(Tables.NETWORK, Tables.NETWORK.ID);
        map.put(Tables.POINT, Tables.POINT.ID);
        map.put(Tables.THING, Tables.THING.ID);
        return Single.fromCallable(() -> createDefaultUUID(map))
                     .doOnSuccess(i -> logger.info("Updated {} tables with random_uuid function", map.size()))
                     .flatMap(i -> initDataFromConfig(EventAction.INIT))
                     .doOnSuccess(ignore -> new DataCacheInitializer().init(this));
    }

    @Override
    public Single<EventMessage> migrate() {
        return EdgeMetadata.INSTANCE.dao(this)
                                    .findOneByCondition(DSL.trueCondition())
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .map(this::cacheEdge)
                                    .map(edge -> EventMessage.initial(EventAction.MIGRATE))
                                    .switchIfEmpty(initDataFromConfig(EventAction.MIGRATE))
                                    .doOnSuccess(ignore -> new DataCacheInitializer().init(this));
    }

    private Single<EventMessage> initDataFromConfig(EventAction action) {
        Map<EntityMetadata, Integer> dep = DataPointIndex.dependencies();
        JsonObject cfgData = configData();
        final Edge edge = cacheEdge(initEdge(cfgData));
        final JsonObject data = cfgData.put(EdgeMetadata.INSTANCE.singularKeyName(), edge.toJson())
                                       .put(NetworkMetadata.INSTANCE.singularKeyName(),
                                            initNetwork(cfgData, edge.getId()));
        return Single.merge(index().stream()
                                   .filter(meta -> !(meta instanceof PointCompositeMetadata) &&
                                                   data.containsKey(meta.singularKeyName()))
                                   .sorted(Comparator.comparingInt(m -> dep.getOrDefault(m, 999)))
                                   .map(m -> insert(m, data.getValue(m.singularKeyName())))
                                   .collect(Collectors.toList()))
                     .buffer(5)
                     .reduce(0, (i, r) -> i + r.stream().reduce(0, Integer::sum))
                     .doOnSuccess(r -> syncData(action, edge))
                     .map(r -> EventMessage.success(action, new JsonObject().put("records", r)));
    }

    private JsonObject configData() {
        JsonObject data = SharedDataDelegate.removeLocalDataValue(vertx(), getSharedKey(), BUILTIN_DATA);
        return Optional.ofNullable(data).orElseGet(JsonObject::new);
    }

    private Edge initEdge(JsonObject builtinData) {
        JsonObject obj = Functions.getIfThrow(
            () -> JsonData.tryParse(builtinData.getValue(EdgeMetadata.INSTANCE.singularKeyName())))
                                  .map(JsonData::toJson)
                                  .orElse(new JsonObject());
        final @NonNull Edge edge = EdgeMetadata.INSTANCE.onCreating(RequestData.builder().body(obj).build());
        JsonObject syncCfg = SharedDataDelegate.removeLocalDataValue(vertx(), getSharedKey(), DATA_SYNC_CFG);
        //TODO fix hard-code version
        syncCfg = new JsonObject().put(DataSyncConfig.NAME,
                                       DataSyncConfig.update(Optional.ofNullable(syncCfg).orElse(new JsonObject()),
                                                             "1.0.0", edge.getId()));
        return edge.setMetadata(
            syncCfg.mergeIn(Optional.ofNullable(edge.getMetadata()).orElse(new JsonObject()), true));
    }

    private JsonArray initNetwork(@NonNull JsonObject builtinData, @NonNull UUID edgeId) {
        final UUID networkId = UUID.randomUUID();
        final Object value = builtinData.getValue(NetworkMetadata.INSTANCE.singularKeyName());
        final JsonObject defaultNetwork = new Network().setId(networkId).setCode("DEFAULT").setEdge(edgeId).toJson();
        if (parsable(Network.class, value)) {
            Network network = EntityHandler.parse(Network.class, value);
            if ("DEFAULT".equals(network.getCode())) {
                network.setEdge(edgeId).setId(networkId);
                return new JsonArray().add(network.toJson());
            }
            return new JsonArray().add(network.toJson()).add(defaultNetwork);
        }
        final Stream<Object> stream = getArrayStream(value);
        if (Objects.nonNull(stream)) {
            final JsonArray array = stream.filter(o -> parsable(Network.class, value))
                                          .map(o -> JsonData.tryParse(o).toJson())
                                          .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
            return array.add(defaultNetwork);
        }
        return new JsonArray().add(defaultNetwork);
    }

    @SuppressWarnings("unchecked")
    private Single<Integer> insert(EntityMetadata metadata, @NonNull Object data) {
        final String createdBy = "SYSTEM_INITIATOR";
        Class<? extends VertxPojo> pClazz = metadata.modelClass();
        VertxDAO dao = metadata.dao(this);
        if (parsable(pClazz, data)) {
            return ((Single<Integer>) dao.insert(
                AuditDecorator.addCreationAudit(true, validate(metadata, data), createdBy))).doOnSuccess(
                logInsertEvent(pClazz));
        }
        final Stream<Object> stream = getArrayStream(data);
        if (Objects.isNull(stream)) {
            return Single.just(0);
        }
        return ((Single<Integer>) dao.insert(stream.filter(o -> parsable(pClazz, o))
                                                   .map(o -> validate(metadata, o))
                                                   .map(pojo -> AuditDecorator.addCreationAudit(true, pojo, createdBy))
                                                   .collect(Collectors.toList()))).doOnSuccess(logInsertEvent(pClazz));
    }

    private VertxPojo validate(EntityMetadata metadata, @NonNull Object data) {
        return metadata.onCreating(RequestData.builder().body(JsonData.tryParse(data).toJson()).build());
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> getArrayStream(Object data) {
        if (data instanceof JsonArray || data instanceof Collection) {
            return data instanceof JsonArray ? ((JsonArray) data).stream() : ((Collection) data).stream();
        }
        return null;
    }

    private boolean parsable(@NonNull Class<? extends VertxPojo> pojoClass, Object data) {
        return data instanceof JsonObject || data instanceof Map || pojoClass.isInstance(data);
    }

    private Consumer<Integer> logInsertEvent(@NonNull Class<? extends VertxPojo> pojoClass) {
        return r -> logger.info("Inserted {} record(s) in {}", r, pojoClass.getSimpleName());
    }

    private void syncData(EventAction action, Edge edge) {
        SyncServiceFactory.getInitialTask(this, sharedData(DATA_SYNC_CFG))
                          .ifPresent(task -> TaskExecuter.asyncExecute(task, getTaskData(action, edge)));
    }

    private EntityTaskData<Edge> getTaskData(EventAction action, Edge edge) {
        return EntityTaskData.<Edge>builder().originReqAction(action)
                                             .originReqData(RequestData.builder().build())
                                             .metadata(EdgeMetadata.INSTANCE)
                                             .data(edge)
                                             .build();
    }

    private Edge cacheEdge(@NonNull Edge edge) {
        addSharedData(EDGE_ID, edge.getId().toString());
        addSharedData(CUSTOMER_CODE, edge.getCustomerCode());
        addSharedData(SITE_CODE, edge.getSiteCode());
        addSharedData(DATA_SYNC_CFG,
                      DataSyncConfig.update(edge.getMetadata().getJsonObject(DataSyncConfig.NAME, new JsonObject()),
                                            "1.0.0", edge.getId()));
        return edge;
    }

}
