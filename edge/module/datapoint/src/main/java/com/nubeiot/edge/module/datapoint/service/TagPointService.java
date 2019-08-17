package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;

import lombok.NonNull;

public final class TagPointService extends AbstractDataPointService<TagPointMetadata, TagPointService> implements
                                                                                                       OneToManyReferenceEntityService<TagPointMetadata, TagPointService>,
                                                                                                       ReferenceEntityTransformer,
                                                                                                       PointExtension {

    public TagPointService(@NonNull AbstractEntityHandler entityHandler) {
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

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public @NonNull ReferenceQueryExecutor queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
