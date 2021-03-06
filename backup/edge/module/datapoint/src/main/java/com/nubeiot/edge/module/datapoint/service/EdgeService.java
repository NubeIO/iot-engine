package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.workflow.step.CreationStep;
import com.nubeiot.core.sql.workflow.step.ModificationStep;
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

    @Override
    protected CreationStep initCreationStep() {
        return super.initCreationStep().onSuccess((action, keyPojo) -> cacheConfig((Edge) keyPojo.dbEntity()));
    }

    @Override
    protected ModificationStep initModificationStep(EventAction action) {
        return super.initModificationStep(action)
                    .onSuccess((reqData, act, keyPojo) -> cacheConfig((Edge) keyPojo.dbEntity()));
    }

    private void cacheConfig(Edge edge) {
        final JsonObject syncConfig = Optional.ofNullable(edge.getMetadata())
                                              .map(info -> info.getJsonObject(DataSyncConfig.NAME, new JsonObject()))
                                              .orElse(new JsonObject());
        entityHandler().addSharedData(DataPointIndex.DATA_SYNC_CFG, syncConfig);
    }

}
