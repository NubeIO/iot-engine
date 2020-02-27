package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.workflow.task.EntityDefinitionContext;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointReferencedTask implements EntityTask<EntityDefinitionContext, PointComposite, PointComposite> {

    @Getter
    private final EntityDefinitionContext definitionContext;
    private final PointReferencedService referencedService;
    private final PointOneToOneService oneToOneService;

    PointReferencedTask(@NonNull EntityHandler entityHandler) {
        this.definitionContext = EntityDefinitionContext.create(entityHandler);
        this.referencedService = new PointReferencedService(entityHandler);
        this.oneToOneService = new PointOneToOneService(entityHandler);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final EventAction action = executionContext.getOriginReqAction();
        if (executionContext.isError() || !(action == EventAction.CREATE || action == EventAction.GET_ONE)) {
            return Single.just(false);
        }
        final RequestData reqData = executionContext.getOriginReqData();
        if (action == EventAction.GET_ONE) {
            final Set<String> dependantKeys = Stream.of(referencedService.dependantEntities().keys(),
                                                        oneToOneService.dependantEntities().keys())
                                                    .flatMap(Collection::stream)
                                                    .map(EntityMetadata::singularKeyName)
                                                    .collect(Collectors.toSet());
            return Single.just(Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                     .anyMatch(dependantKeys::contains));
        }
        return Single.just(oneToOneService.dependantEntities()
                                          .keys()
                                          .stream()
                                          .anyMatch(m -> reqData.body().containsKey(m.singularKeyName())));
    }

    @Override
    public @NonNull Maybe<PointComposite> execute(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        return executionContext.getOriginReqAction() == EventAction.GET_ONE
               ? onGet(executionContext)
               : onCreate(executionContext);
    }

    private Maybe<PointComposite> onCreate(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final PointComposite pojo = executionContext.getData();
        return oneToOneService.create(executionContext.getOriginReqData(), pojo, pojo.getId());
    }

    private Maybe<PointComposite> onGet(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final RequestData reqData = executionContext.getOriginReqData();
        final PointComposite pojo = executionContext.getData();
        final Set<String> dependants = Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                             .collect(Collectors.toSet());
        return referencedService.get(pojo, pojo.getId(), dependants)
                                .flatMap(p -> oneToOneService.get(p, p.getId(), dependants));
    }

}
