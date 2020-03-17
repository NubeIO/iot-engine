package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.ReferencedEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class PointReferencedService implements ReferencedEntityService<PointComposite, PointCompositeMetadata> {

    private final EntityHandler entityHandler;

    @Override
    public PointCompositeMetadata context() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityReferences dependantEntities() {
        return new EntityReferences().add(TagPointMetadata.INSTANCE, "point");
    }

    @Override
    public @NonNull Map<EntityMetadata, ReferenceServiceMetadata> dependantServices() {
        final String address = TagPointService.class.getName();
        return Collections.singletonMap(TagPointMetadata.INSTANCE,
                                        ReferenceServiceMetadata.builder().address(address).build());
    }

}
