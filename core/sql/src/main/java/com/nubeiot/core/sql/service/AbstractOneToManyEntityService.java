package com.nubeiot.core.sql.service;

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
    @NonNull
    public RequestData onHandlingManyResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, null);
    }

    @Override
    @NonNull
    public RequestData onHandlingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, context()));
    }

    @Override
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        return validate(reqData).flatMapSingle(b -> super.doInsert(reqData));
    }

    protected Maybe<Boolean> validate(@NonNull RequestData reqData) {
        return queryExecutor().mustExists(reqData, ref().entityReferences().refMetadata());
    }

    protected RequestData recomputeRequestData(@NonNull RequestData requestData, JsonObject extra) {
        JsonObject body = Optional.ofNullable(requestData.body()).orElseGet(JsonObject::new);
        JsonObject filter = new JsonObject(entityReferences().computeRequest(body));
        Optional.ofNullable(extra).ifPresent(e -> filter.getMap().putAll(e.getMap()));
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
