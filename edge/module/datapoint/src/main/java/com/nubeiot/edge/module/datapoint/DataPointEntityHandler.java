package com.nubeiot.edge.module.datapoint;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Configuration;

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
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.workflow.EntityTaskExecuter.AsyncEntityTaskExecuter;
import com.nubeiot.core.sql.workflow.task.EntityTaskData;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.task.sync.SyncServiceFactory;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.Keys;
import com.nubeiot.iotdata.edge.model.tables.interfaces.INetwork;
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
    public @NonNull SchemaHandler schemaHandler() {
        return new DataPointSchemaHandler();
    }

    Single<JsonObject> initDataFromConfig(EventAction action) {
        final Map<EntityMetadata, Integer> dep = DataPointIndex.dependencies();
        final JsonObject cfgData = configData();
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
                     .map(r -> new JsonObject().put("records", r));
    }

    Edge cacheEdge(@NonNull Edge edge) {
        addSharedData(EDGE_ID, edge.getId().toString());
        addSharedData(CUSTOMER_CODE, edge.getCustomerCode());
        addSharedData(SITE_CODE, edge.getSiteCode());
        addSharedData(DATA_SYNC_CFG,
                      DataSyncConfig.update(edge.getMetadata().getJsonObject(DataSyncConfig.NAME, new JsonObject()),
                                            "1.0.0", edge.getId()));
        return edge;
    }

    private JsonObject configData() {
        final JsonObject data = SharedDataDelegate.removeLocalDataValue(vertx(), getSharedKey(), BUILTIN_DATA);
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
        final Object value = builtinData.getValue(NetworkMetadata.INSTANCE.singularKeyName());
        final UUID networkId = UUID.fromString(addSharedData(DEFAULT_NETWORK_ID, UUID.randomUUID().toString()));
        final Network defaultNetwork = new Network().setId(networkId)
                                                    .setCode(NetworkMetadata.DEFAULT_CODE)
                                                    .setProtocol(Protocol.WIRE)
                                                    .setState(State.ENABLED)
                                                    .setEdge(edgeId);
        if (parsable(Network.class, value)) {
            final Network network = checkNetworkIsDefault(edgeId, networkId, EntityHandler.parse(Network.class, value));
            final JsonArray array = new JsonArray().add(network.toJson());
            return network.getCode().equals(NetworkMetadata.DEFAULT_CODE) ? array : array.add(defaultNetwork.toJson());
        }
        final Stream<Object> stream = getArrayStream(value);
        if (Objects.nonNull(stream)) {
            final JsonArray networks = stream.filter(o -> parsable(Network.class, o))
                                             .map(o -> EntityHandler.parse(Network.class, o))
                                             .map(n -> checkNetworkIsDefault(edgeId, networkId, n))
                                             .map(INetwork::toJson)
                                             .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
            if (networks.stream()
                        .map(JsonObject.class::cast)
                        .anyMatch(network -> network.getString("code").equals(NetworkMetadata.DEFAULT_CODE))) {
                return networks;
            }
            return networks.add(defaultNetwork.toJson());
        }
        return new JsonArray().add(defaultNetwork.toJson());
    }

    private Network checkNetworkIsDefault(@NonNull UUID edgeId, @NonNull UUID networkId, @NonNull Network network) {
        if (!NetworkMetadata.DEFAULT_CODE.equals(network.getCode())) {
            return network;
        }
        final UUID id = Optional.ofNullable(network.getId()).orElse(networkId);
        addSharedData(DEFAULT_NETWORK_ID, id.toString());
        return network.setEdge(edgeId).setId(id);
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

    private void syncData(@NonNull EventAction action, @NonNull Edge edge) {
        SyncServiceFactory.getInitialTask(this, sharedData(DATA_SYNC_CFG))
                          .map(AsyncEntityTaskExecuter::create)
                          .ifPresent(wf -> wf.execute(EntityTaskData.builder()
                                                                    .originReqAction(action)
                                                                    .originReqData(RequestData.builder().build())
                                                                    .metadata(EdgeMetadata.INSTANCE)
                                                                    .data(edge)
                                                                    .build()));
    }

}
