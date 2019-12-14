package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.core.sql.service.TransitiveReferenceMarker;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.ThingComposite;
import com.nubeiot.iotdata.dto.ThingType;
import com.nubeiot.iotdata.edge.model.tables.PointThing;

import lombok.NonNull;

public final class PointByThingService extends AbstractManyToManyEntityService<ThingComposite, PointThingMetadata>
    implements DataPointService<ThingComposite, PointThingMetadata>, TransitiveReferenceMarker {

    public PointByThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    public static String genComputedThing(@NonNull ThingType type, @NonNull UUID thingId) {
        if (type != ThingType.SENSOR) {
            return null;
        }
        return UUID64.uuidToBase64(thingId) + "-" + type.type();
    }

    @Override
    public PointThingMetadata context() {
        return PointThingMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return ThingMetadata.INSTANCE;
    }

    @Override
    public @NonNull List<EntityMetadata> references() {
        return Arrays.asList(ThingMetadata.INSTANCE, DeviceMetadata.INSTANCE, NetworkMetadata.INSTANCE);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), reference());
    }

    @Override
    public Set<String> ignoreFields(@NonNull RequestData requestData) {
        final @NonNull PointThing table = context().table();
        final Set<String> ignores = super.ignoreFields(requestData);
        ignores.add(table.getJsonField(table.DEVICE_ID));
        ignores.add(table.getJsonField(table.NETWORK_ID));
        ignores.add(table.getJsonField(table.EDGE_ID));
        ignores.add(table.getJsonField(table.COMPUTED_THING));
        return ignores;
    }

    @Override
    public @NonNull Map<EntityMetadata, TransitiveEntity> transitiveReferences() {
        return null;
    }

}
