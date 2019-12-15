package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.ThingComposite;
import com.nubeiot.iotdata.edge.model.tables.PointThing;

import lombok.NonNull;

public final class PointByThingService extends AbstractManyToManyEntityService<ThingComposite, PointThingMetadata>
    implements DataPointService<ThingComposite, PointThingMetadata> {

    public PointByThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
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
        final @NonNull Collection<EventAction> events = getAvailableEvents();
        return Stream.of(EntityHttpService.createDefinitions(events, resource(), true, NetworkMetadata.INSTANCE,
                                                             DeviceMetadata.INSTANCE, ThingMetadata.INSTANCE),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(), true,
                                                             NetworkMetadata.INSTANCE, DeviceMetadata.INSTANCE),
                         EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, events, resource(),
                                                             DeviceMetadata.INSTANCE),
                         EntityHttpService.createDefinitions(events, resource(), DeviceMetadata.INSTANCE,
                                                             ThingMetadata.INSTANCE))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
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
    protected RequestData recomputeRequestData(RequestData reqData, JsonObject extra) {
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.filter());
        return super.recomputeRequestData(reqData, extra);
    }

}
