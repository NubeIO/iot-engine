package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.OneToOneEntityService;
import com.nubeiot.core.sql.service.ReferencedEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.workflow.task.EntityDefinitionContext;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointReferencedService
    implements EntityTask<EntityDefinitionContext, PointComposite, PointComposite>,
               ReferencedEntityService<PointComposite, PointCompositeMetadata> {

    @Getter
    private final EntityDefinitionContext definitionContext;
    private final PointOneToOneService oneToOneService;

    PointReferencedService(@NonNull EntityHandler entityHandler) {
        this.definitionContext = EntityDefinitionContext.create(entityHandler);
        this.oneToOneService = new PointOneToOneService(entityHandler);
    }

    @Override
    public @NonNull EntityHandler entityHandler() {
        return definitionContext.entityHandler();
    }

    @Override
    public PointCompositeMetadata context() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityReferences dependantEntities() {
        return new EntityReferences().add(TagPointMetadata.INSTANCE, "point");
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final EventAction action = executionContext.getOriginReqAction();
        if (executionContext.isError() || !(action == EventAction.CREATE || action == EventAction.GET_ONE)) {
            return Single.just(false);
        }
        if (action == EventAction.GET_ONE) {
            final RequestData reqData = executionContext.getOriginReqData();
            final Set<String> dependantKeys = dependantEntities().keys()
                                                                 .stream()
                                                                 .map(EntityMetadata::singularKeyName)
                                                                 .collect(Collectors.toSet());
            return Single.just(Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                     .anyMatch(dependantKeys::contains));
        }
        final PointComposite point = executionContext.getData();
        return Single.just(oneToOneService.dependantEntities()
                                          .keys()
                                          .stream()
                                          .map(m -> point.safeGetOther(m.singularKeyName(),
                                                                       (Class<VertxPojo>) m.modelClass()))
                                          .anyMatch(Objects::nonNull));
    }

    @Override
    public @NonNull Maybe<PointComposite> execute(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        return executionContext.getOriginReqAction() == EventAction.GET_ONE
               ? onGet(executionContext)
               : onCreate(executionContext);
    }

    //TODO Must implement Database Transaction to finish it
    private Maybe<PointComposite> onCreate(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        return oneToOneService.onCreate(executionContext.getOriginReqData(), executionContext.getData());
    }

    private Maybe<PointComposite> onGet(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final RequestData reqData = executionContext.getOriginReqData();
        final PointComposite pojo = executionContext.getData();
        final Set<String> dependants = Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                             .collect(Collectors.toSet());
        return onGet(pojo, pojo.getId(), dependants).flatMap(p -> oneToOneService.onGet(p, p.getId(), dependants));
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PointOneToOneService implements OneToOneEntityService<PointComposite, PointCompositeMetadata> {

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

    }

}

