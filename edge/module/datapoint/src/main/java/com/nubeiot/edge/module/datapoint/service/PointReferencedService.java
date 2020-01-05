package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.ReferencedEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;
import com.nubeiot.core.sql.workflow.task.EntityDefinitionContext;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TagPointMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointReferencedService
    implements ReferencedEntityService<PointCompositeMetadata>, OneToOneEntityMarker,
               EntityTask<EntityDefinitionContext, PointComposite, PointComposite> {

    @Getter
    private final EntityDefinitionContext definitionContext;

    PointReferencedService(@NonNull EntityHandler entityHandler) {
        this.definitionContext = EntityDefinitionContext.create(entityHandler);
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
        return new EntityReferences().add(PointValueMetadata.INSTANCE)
                                     .add(HistorySettingMetadata.INSTANCE)
                                     .add(RealtimeSettingMetadata.INSTANCE)
                                     .add(TagPointMetadata.INSTANCE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final EventAction action = executionContext.getOriginReqAction();
        if (executionContext.isError() || !(action == EventAction.CREATE || action == EventAction.GET_ONE)) {
            return Single.just(false);
        }
        final Set<EntityMetadata> list = dependantEntities().getFields().keySet();
        if (action == EventAction.GET_ONE) {
            final RequestData reqData = executionContext.getOriginReqData();
            final Set<String> dependantKeys = list.stream()
                                                  .map(EntityMetadata::singularKeyName)
                                                  .collect(Collectors.toSet());
            return Single.just(Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                     .anyMatch(dependantKeys::contains));
        }
        final PointComposite point = executionContext.getData();
        return Single.just(list.stream()
                               .filter(allowCreation())
                               .map(m -> point.safeGetOther(m.singularKeyName(), (Class<VertxPojo>) m.modelClass()))
                               .anyMatch(Objects::nonNull));
    }

    @Override
    public @NonNull Maybe<PointComposite> execute(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        return executionContext.getOriginReqAction() == EventAction.GET_ONE
               ? onGet(executionContext)
               : onCreate(executionContext);
    }

    @Override
    public @NonNull Predicate<EntityMetadata> allowCreation() {
        return metadata -> PointValueMetadata.INSTANCE == metadata;
    }

    private Maybe<PointComposite> onCreate(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        return Maybe.just(executionContext.getData());
    }

    private Maybe<PointComposite> onGet(@NonNull EntityRuntimeContext<PointComposite> executionContext) {
        final RequestData reqData = executionContext.getOriginReqData();
        final Set<String> requestEntities = Arrays.stream(reqData.filter().getString(Filters.INCLUDE, "").split(","))
                                                  .collect(Collectors.toSet());
        return Observable.fromIterable(dependantEntities().getFields().keySet())
                         .filter(metadata -> requestEntities.contains(metadata.singularKeyName()))
                         .flatMapMaybe(metadata -> find(metadata, executionContext.getData()))
                         .reduce((p1, p2) -> p2);
    }

    private Maybe<PointComposite> find(@NonNull EntityMetadata metadata, @NonNull PointComposite point) {
        if (allowCreation().test(metadata)) {
            return referencedQuery(metadata).findOneByKey(
                RequestData.builder().body(new JsonObject().put("point_id", point.getId().toString())).build())
                                            .map(r -> point.put(metadata.singularKeyName(), r))
                                            .onErrorReturn(t -> point)
                                            .toMaybe();
        }
        return referencedQuery(metadata).findMany(
            RequestData.builder().filter(new JsonObject().put("point", point.getId().toString())).build())
                                        .toList()
                                        .filter(l -> !l.isEmpty())
                                        .map(l -> point.put(metadata.pluralKeyName(), l));
    }

}

