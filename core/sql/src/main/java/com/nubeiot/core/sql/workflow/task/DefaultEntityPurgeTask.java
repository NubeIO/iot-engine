package com.nubeiot.core.sql.workflow.task;

import java.util.List;

import org.jooq.Condition;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.ReferenceEntityMetadata;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.service.EntityApiService;
import com.nubeiot.core.sql.workflow.task.EntityTask.EntityPurgeTask;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("unchecked")
final class DefaultEntityPurgeTask<P extends VertxPojo> implements EntityPurgeTask<PurgeDefinitionContext, P, DMLPojo> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPurgeTask.class.getName());
    @NonNull
    private final PurgeDefinitionContext definitionContext;

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<P> runtimeContext) {
        return Single.just(runtimeContext.getOriginReqAction() == EventAction.REMOVE);
    }

    @Override
    public @NonNull Maybe<DMLPojo> execute(@NonNull EntityRuntimeContext<P> runtimeContext) {
        final @NonNull EntityMetadata metadata = runtimeContext.getMetadata();
        final @NonNull RequestData reqData = runtimeContext.getOriginReqData();
        final P pojo = runtimeContext.getData();
        final Object pk = metadata.parseKey(pojo);
        final DMLPojo dmlPojo = DMLPojo.builder().request(pojo).primaryKey(pk).build();
        if (!reqData.filter().hasForce()) {
            return definitionContext.queryExecutor().isAbleToDelete(pojo, metadata).map(b -> dmlPojo).toMaybe();
        }
        final EntityApiService apiService = apiService();
        return Observable.fromIterable(definitionContext.entityHandler().holder().referenceTo(metadata))
                         .flatMapSingle(ref -> invokeReferenceService(apiService, reqData, ref, pk))
                         .reduce(Long::sum)
                         .map(t -> new JsonObject().put("total", t))
                         .map(r -> dmlPojo)
                         .defaultIfEmpty(dmlPojo);
    }

    private Single<Long> invokeReferenceService(@NonNull EntityApiService apiService, @NonNull RequestData reqData,
                                                @NonNull ReferenceEntityMetadata ref, @NonNull Object pk) {
        final EntityMetadata refMetadata = ref.findByTable(definitionContext().entityHandler().metadataIndex());
        final Condition eq = ref.getField().eq(pk);
        final Single<List<VertxPojo>> result = (Single<List<VertxPojo>>) definitionContext().queryExecutor()
                                                                                            .dao(refMetadata)
                                                                                            .findManyByCondition(eq);
        return result.flattenAsObservable(rs -> rs)
                     .map(refMetadata::parseKey)
                     .map(rpk -> RequestData.builder()
                                            .filter(reqData.filter())
                                            .body(new JsonObject().put(refMetadata.requestKeyName(),
                                                                       JsonData.checkAndConvert(rpk)))
                                            .build())
                     .flatMapSingle(req -> transporter().request(apiService.lookupApiName(refMetadata),
                                                                 EventMessage.initial(EventAction.REMOVE, req)))
                     .map(EventMessage::getData)
                     .count();
    }

}
