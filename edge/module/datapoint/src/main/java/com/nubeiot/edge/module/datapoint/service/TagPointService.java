package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;

import lombok.NonNull;

public final class TagPointService extends AbstractOneToManyEntityService<PointTag, TagPointMetadata>
    implements PointExtension, DataPointService<PointTag, TagPointMetadata> {

    public TagPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             Stream.of(EventMethodDefinition.createDefault("/point/:point_id/tags", "/:tag_id")))
                     .collect(Collectors.toSet());
    }

    @Override
    public String servicePath() {
        return "/tags";
    }

    @Override
    public TagPointMetadata context() {
        return TagPointMetadata.INSTANCE;
    }

}
