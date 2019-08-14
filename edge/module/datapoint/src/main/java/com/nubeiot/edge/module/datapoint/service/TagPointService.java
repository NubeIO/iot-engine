package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.edge.model.tables.records.PointTagRecord;

import lombok.NonNull;

public final class TagPointService
    extends AbstractDataPointService<Long, PointTag, PointTagRecord, PointTagDao, TagPointMetadata>
    implements OneToManyReferenceEntityService<Long, PointTag, PointTagRecord, PointTagDao, TagPointMetadata>,
               PointExtension {

    public TagPointService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(super.definitions().stream(),
                             Stream.of(EventMethodDefinition.createDefault("/point/:point_id/tags", "/:tag_id")))
                     .collect(Collectors.toSet());
    }

    @Override
    public String servicePath() {
        return "/tags";
    }

    @Override
    public TagPointMetadata metadata() {
        return TagPointMetadata.INSTANCE;
    }

}
