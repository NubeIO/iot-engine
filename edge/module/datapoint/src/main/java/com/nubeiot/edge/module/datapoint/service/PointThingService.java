package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;
import com.nubeiot.iotdata.edge.model.tables.PointThing;

import lombok.NonNull;

abstract class PointThingService extends AbstractManyToManyEntityService<PointThingComposite, PointThingMetadata>
    implements DataPointService<PointThingComposite, PointThingMetadata> {

    PointThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public Set<String> ignoreFields() {
        final @NonNull PointThing table = context().table();
        return Stream.of(super.ignoreFields(),
                         Arrays.asList(table.getJsonField(table.DEVICE_ID), table.getJsonField(table.NETWORK_ID),
                                       table.getJsonField(table.EDGE_ID), table.getJsonField(table.COMPUTED_THING)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    protected RequestData recomputeRequestData(RequestData reqData, JsonObject extra) {
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(reqData.filter());
        return super.recomputeRequestData(reqData, extra);
    }

}
