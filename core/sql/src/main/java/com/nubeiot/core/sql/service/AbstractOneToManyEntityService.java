package com.nubeiot.core.sql.service;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferenceEntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;

import lombok.NonNull;

public abstract class AbstractOneToManyEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractEntityService<P, M>
    implements OneToManyReferenceEntityService<P, M>, ReferenceEntityTransformer, HasReferenceResource {

    public AbstractOneToManyEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull ReferenceQueryExecutor<P> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData,
                                    convertKey(requestData, ref().entityReferences().getFields().entrySet().stream()));
    }

    @Override
    @NonNull
    public RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final JsonObject refExtra = convertKey(requestData, ref().entityReferences().getFields().entrySet().stream());
        return recomputeRequestData(requestData, extra.mergeIn(refExtra, true));
    }

    @Override
    @NonNull
    public RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, null);
    }

    @Override
    @NonNull
    public RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, context()));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        final P p = (P) validation().onCreating(reqData);
        return validateReferenceEntity(reqData).flatMapSingle(b -> queryExecutor().insertReturningPrimary(p, reqData));
    }

    protected Maybe<Boolean> validateReferenceEntity(@NonNull RequestData reqData) {
        return queryExecutor().mustExists(reqData, ref());
    }

    protected RequestData recomputeRequestData(@NonNull RequestData requestData, JsonObject extra) {
        JsonObject body = Optional.ofNullable(requestData.body()).orElseGet(JsonObject::new);
        JsonObject filter = new JsonObject(entityReferences().computeRequest(body));
        Optional.ofNullable(extra).ifPresent(e -> filter.mergeIn(e, true));
        body = body.mergeIn(filter, true);
        final JsonObject combineFilter = Objects.isNull(requestData.getFilter())
                                         ? filter
                                         : requestData.getFilter().mergeIn(filter, true);
        return RequestData.builder()
                          .body(body)
                          .headers(requestData.headers())
                          .filter(combineFilter)
                          .pagination(requestData.getPagination())
                          .build();
    }

    JsonObject convertKey(@NonNull RequestData reqData, @NonNull Stream<Entry<EntityMetadata, String>> ref) {
        JsonObject body = reqData.body();
        JsonObject extra = new JsonObject();
        ref.filter(entry -> !body.containsKey(entry.getKey().singularKeyName()) && body.containsKey(entry.getValue()))
           .forEach(entry -> extra.put(entry.getKey().singularKeyName(),
                                       new JsonObject().put(entry.getKey().jsonKeyName(),
                                                            body.getValue(entry.getValue()))));
        return extra;
    }

    JsonObject convertKey(@NonNull RequestData requestData, EntityMetadata... metadata) {
        JsonObject object = new JsonObject();
        Stream.of(metadata)
              .filter(Objects::nonNull)
              .forEach(meta -> object.put(context().requestKeyName().equals(meta.requestKeyName())
                                          ? context().jsonKeyName()
                                          : meta.requestKeyName(),
                                          JsonData.checkAndConvert(meta.parseKey(requestData))));
        return object;
    }

}
