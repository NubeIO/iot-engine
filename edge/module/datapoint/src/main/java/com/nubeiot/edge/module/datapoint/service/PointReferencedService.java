package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.ReferencedEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.core.sql.workflow.task.EntityTaskContext;
import com.nubeiot.core.sql.workflow.task.EntityTaskData;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.edge.module.datapoint.service.PointReferencedService.PointReferencedContext;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public final class PointReferencedService
    implements ReferencedEntityService<Point, PointMetadata, PointComposite, PointCompositeMetadata>,
               OneToOneEntityMarker, EntityTask<PointReferencedContext, PointComposite, PointComposite> {

    @Getter
    private final PointReferencedContext taskContext;

    PointReferencedService(@NonNull EntityHandler entityHandler) {
        this.taskContext = new PointReferencedContext(entityHandler);
    }

    @Override
    public PointCompositeMetadata referencedContext() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityHandler entityHandler() {
        return taskContext.entityHandler();
    }

    @Override
    public PointMetadata context() {
        return PointMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityReferences dependantEntities() {
        return new EntityReferences().add(PointValueMetadata.INSTANCE).add(HistorySettingMetadata.INSTANCE);
    }

    @Override
    public PointReferencedContext definition() {
        return taskContext;
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityTaskData<PointComposite> executionData) {
        final EventAction action = executionData.getOriginReqAction();
        if (executionData.isError() || !(action == EventAction.CREATE || action == EventAction.GET_ONE)) {
            return Single.just(false);
        }
        if (action == EventAction.GET_ONE) {
            final RequestData reqData = executionData.getOriginReqData();
            final Set<String> dependantKeys = dependantEntities().getFields()
                                                                 .keySet()
                                                                 .stream()
                                                                 .map(EntityMetadata::singularKeyName)
                                                                 .collect(Collectors.toSet());
            return Single.just(Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                     .anyMatch(dependantKeys::contains));
        }
        final PointComposite point = executionData.getData();
        return Single.just(dependantEntities().getFields()
                                              .keySet()
                                              .stream()
                                              .filter(allowCreation())
                                              .map(m -> point.safeGetOther(m.singularKeyName(),
                                                                           (Class<VertxPojo>) m.modelClass()))
                                              .anyMatch(Objects::nonNull));
    }

    @Override
    public @NonNull Maybe<PointComposite> execute(@NonNull EntityTaskData<PointComposite> executionData) {
        return Maybe.just(executionData.getData());
    }

    @Override
    public @NonNull Predicate<EntityMetadata> allowCreation() {
        return metadata -> PointValueMetadata.INSTANCE == metadata;
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true)
    public static final class PointReferencedContext implements EntityTaskContext<EventbusClient> {

        @Getter
        private final EntityHandler entityHandler;

        @Override
        public EventbusClient transporter() {
            return entityHandler.eventClient();
        }

    }

}

