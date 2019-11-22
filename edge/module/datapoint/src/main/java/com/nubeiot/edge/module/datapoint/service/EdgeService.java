package com.nubeiot.edge.module.datapoint.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Edge;

import lombok.NonNull;

public final class EdgeService extends AbstractEntityService<Edge, EdgeMetadata>
    implements DataPointService<Edge, EdgeMetadata> {

    public EdgeService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EdgeMetadata context() {
        return EdgeMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.PATCH);
    }

    protected Single<Entry<?, ? extends VertxPojo>> afterCreateOrUpdate(@NonNull RequestData reqData,
                                                                        @NonNull EventAction action,
                                                                        @NonNull Object primaryKey) {
        return doLookupByPrimaryKey(primaryKey).doOnSuccess(pojo -> cacheConfig((Edge) pojo))
                                               .doOnEvent((p, e) -> invokeAsyncTask(reqData, action, p, e))
                                               .map(pojo -> new SimpleEntry<>(primaryKey, pojo));
    }

    private void cacheConfig(Edge edge) {
        final JsonObject syncConfig = Optional.ofNullable(edge.getMetadata())
                                              .map(info -> info.getJsonObject(DataSyncConfig.NAME, new JsonObject()))
                                              .orElse(new JsonObject());
        entityHandler().addSharedData(DataPointIndex.DATA_SYNC_CFG, syncConfig);
    }

    public interface EdgeExtension extends HasReferenceResource {

        static void optimizeReqData(@NonNull EntityHandler handler, @NonNull RequestData requestData,
                                    @NonNull String edgeField) {
            final String edgeId = handler.sharedData(DataPointIndex.EDGE_ID);
            if (Objects.nonNull(requestData.body()) && !requestData.body().containsKey(edgeField)) {
                requestData.body().put(edgeField, edgeId);
            }
        }

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(EdgeMetadata.INSTANCE, "edge");
        }

    }

}
