package com.nubeiot.core.sql.workflow.task;

import java.util.List;

import org.jooq.Condition;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;
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
import com.nubeiot.core.sql.cache.EntityServiceIndex;
import com.nubeiot.core.sql.pojos.DMLPojo;
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
    private final boolean supportForceDeletion;

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
        if (!supportForceDeletion || !reqData.filter().hasForce()) {
            return definitionContext.queryExecutor().isAbleToDelete(pojo, metadata).map(b -> dmlPojo).toMaybe();
        }
        final @NonNull EntityServiceIndex index = definitionContext.entityServiceIndex();
        return Observable.fromIterable(definitionContext.entityHandler().holder().referenceTo(metadata))
                         .flatMapSingle(ref -> invokeReferenceService(index, reqData, ref, pk))
                         .reduce(Long::sum)
                         .map(t -> new JsonObject().put("total", t))
                         .map(r -> dmlPojo)
                         .defaultIfEmpty(dmlPojo);
    }

    private Single<Long> invokeReferenceService(@NonNull EntityServiceIndex index, @NonNull RequestData reqData,
                                                @NonNull ReferenceEntityMetadata ref, @NonNull Object pk) {
        final EntityMetadata refMetadata = ref.findByTable(definitionContext().entityHandler().metadataIndex());
        final Condition eq = ref.getField().eq(pk);
        final String address = Functions.getIfThrow(() -> index.lookupApiAddress(refMetadata)).orElse(null);
        if (Strings.isBlank(address)) {
            return deleteDirectly(refMetadata, eq);
        }
        return invokeRemoteDeletion(reqData, refMetadata, eq, address);
    }

    //TODO temporary way to batch delete. Not safe. Must implement ASAP https://github.com/NubeIO/iot-engine/issues/294
    private Single<Long> deleteDirectly(@NonNull EntityMetadata refMetadata, @NonNull Condition eq) {
        LOGGER.debug("Not safe function when purging resources directly");
        return ((Single<Integer>) definitionContext().queryExecutor().dao(refMetadata).deleteByCondition(eq)).map(
            Long::valueOf);
    }

    private Single<Long> invokeRemoteDeletion(@NonNull RequestData reqData, @NonNull EntityMetadata refMetadata,
                                              @NonNull Condition eq, @NonNull String address) {
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
                     .map(req -> EventMessage.initial(EventAction.REMOVE, req))
                     .flatMapSingle(msg -> transporter().request(address, msg))
                     .map(this::handleResult)
                     .count();
    }

    private JsonObject handleResult(@NonNull EventMessage message) {
        LOGGER.debug(message.toJson());
        if (message.isError()) {
            return message.getError().toJson();
        }
        return message.getData();
    }

}
