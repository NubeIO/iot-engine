package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.iotdata.edge.model.tables.EdgeDevice;

import lombok.NonNull;

public final class DeviceByNetworkService
    extends AbstractManyToManyEntityService<EdgeDeviceComposite, EdgeDeviceCompositeMetadata>
    implements DataPointService<EdgeDeviceComposite, EdgeDeviceCompositeMetadata> {

    public DeviceByNetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public EdgeDeviceCompositeMetadata context() {
        return EdgeDeviceCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return super.onCreatingOneResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return super.onModifyingOneResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return super.onReadingManyResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return super.onReadingOneResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public @NonNull List<EntityMetadata> references() {
        return Arrays.asList(reference(), EdgeMetadata.INSTANCE);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Set<String> ignores = super.ignoreFields(requestData);
        final EdgeDevice table = context().table();
        ignores.add(table.getJsonField(table.EDGE_ID));
        return ignores;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), NetworkMetadata.INSTANCE,
                                                   EdgeMetadata.INSTANCE);
    }

    private RequestData optimizeRequestData(@NonNull RequestData requestData) {
        final String edgeId = entityHandler().sharedData(DataPointIndex.EDGE_ID);
        final String edgeField = context().table().getJsonField(context().table().EDGE_ID);
        if (!requestData.body().containsKey(edgeField)) {
            requestData.body().put(edgeField, edgeId);
        }
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.getFilter());
        return requestData;
    }

}
