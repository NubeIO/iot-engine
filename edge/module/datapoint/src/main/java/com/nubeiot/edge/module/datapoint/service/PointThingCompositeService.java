package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;
import com.nubeiot.iotdata.edge.model.tables.PointThing;

import lombok.NonNull;

abstract class PointThingCompositeService
    extends AbstractManyToManyEntityService<PointThingComposite, PointThingMetadata>
    implements DataPointService<PointThingComposite, PointThingMetadata> {

    PointThingCompositeService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
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
