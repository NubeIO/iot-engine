package com.nubeiot.core.sql.service;

import java.util.Map.Entry;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;

import lombok.NonNull;

public abstract class AbstractGroupEntityService<P extends VertxPojo, M extends EntityMetadata,
                                                    CP extends CompositePojo<P, CP>, CM extends CompositeMetadata>
    extends AbstractOneToManyEntityService<P, M>
    implements GroupEntityService<P, M, CP, CM>, GroupEntityTransformer, GroupReferenceResource {

    public AbstractGroupEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    public GroupReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            ref().entityReferences().getFields().entrySet().stream(),
            ref().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, convertKey(requestData, stream));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            ref().entityReferences().getFields().entrySet().stream(),
            ref().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, extra.mergeIn(convertKey(requestData, stream), true));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        final CP p = (CP) contextGroup().onCreating(reqData);
        return validateReferenceEntity(reqData).flatMapSingle(b -> groupQuery().insertReturningPrimary(p, reqData));
    }

    @Override
    protected Maybe<Boolean> validateReferenceEntity(@NonNull RequestData reqData) {
        return groupQuery().mustExists(reqData, ref());
    }

    @Override
    public Single<? extends VertxPojo> doLookupByPrimaryKey(@NonNull Object key) {
        return groupQuery().lookupByPrimaryKey(key);
    }

}
