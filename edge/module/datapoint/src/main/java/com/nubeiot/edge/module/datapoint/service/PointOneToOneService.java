package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.OneToOneParentEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class PointOneToOneService implements OneToOneParentEntityService<PointComposite, PointCompositeMetadata> {

    private final EntityHandler entityHandler;

    @Override
    public PointCompositeMetadata context() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityReferences dependantEntities() {
        return new EntityReferences().add(PointValueMetadata.INSTANCE)
                                     .add(HistorySettingMetadata.INSTANCE)
                                     .add(RealtimeSettingMetadata.INSTANCE);
    }

    @Override
    public @NonNull Map<EntityMetadata, ReferenceServiceMetadata> dependantServices() {
        final Map<EntityMetadata, ReferenceServiceMetadata> m = new HashMap<>();
        m.put(PointValueMetadata.INSTANCE, ReferenceServiceMetadata.builder()
                                                                   .address(PointValueService.class.getName())
                                                                   .action(EventAction.CREATE,
                                                                           EventAction.CREATE_OR_UPDATE)
                                                                   .build());
        m.put(HistorySettingMetadata.INSTANCE, ReferenceServiceMetadata.builder()
                                                                       .address(HistorySettingService.class.getName())
                                                                       .action(EventAction.CREATE,
                                                                               EventAction.CREATE_OR_UPDATE)
                                                                       .build());
        m.put(RealtimeSettingMetadata.INSTANCE, ReferenceServiceMetadata.builder()
                                                                        .address(RealtimeSettingService.class.getName())
                                                                        .action(EventAction.CREATE,
                                                                                EventAction.CREATE_OR_UPDATE)
                                                                        .build());
        return Collections.unmodifiableMap(m);
    }

}
