package com.nubeiot.core.sql.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferenceEntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.Functions;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code one-to-many entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see OneToManyReferenceEntityService
 * @see ReferenceEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractOneToManyEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends AbstractEntityService<P, M> implements OneToManyReferenceEntityService<P, M>, ReferenceEntityTransformer {

    /**
     * Instantiates a new Abstract one to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
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
    public HasReferenceMarker marker() {
        return this;
    }

    protected OperationValidator initCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> queryExecutor().mustExists(req).map(b -> validation().onCreating(req)));
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, marker().entityReferences()
                                                                                 .getFields()
                                                                                 .entrySet()
                                                                                 .stream()));
    }

    @Override
    @NonNull
    public RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final JsonObject refExtra = convertKey(requestData,
                                               marker().entityReferences().getFields().entrySet().stream());
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

    protected RequestData recomputeRequestData(@NonNull RequestData requestData, JsonObject extra) {
        JsonObject body = Optional.ofNullable(requestData.body()).orElseGet(JsonObject::new);
        JsonObject filter = new JsonObject(entityReferences().computeRequest(body));
        Optional.ofNullable(extra).ifPresent(e -> filter.mergeIn(e, true));
        body = body.mergeIn(filter, true);
        final JsonObject combineFilter = Objects.isNull(requestData.filter())
                                         ? filter
                                         : requestData.filter().mergeIn(filter, true);
        return RequestData.builder()
                          .body(body)
                          .headers(requestData.headers())
                          .filter(combineFilter)
                          .sort(requestData.sort())
                          .pagination(requestData.pagination())
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

    JsonObject convertKey(@NonNull RequestData reqData, EntityMetadata... metadata) {
        return convertKey(reqData, Arrays.asList(metadata));
    }

    JsonObject convertKey(@NonNull RequestData reqData, List<EntityMetadata> metadata) {
        JsonObject object = new JsonObject();
        Function<EntityMetadata, Object> valFunc = meta -> JsonData.checkAndConvert(meta.parseKey(reqData));
        Function<EntityMetadata, String> keyFunc = meta -> context().requestKeyName().equals(meta.requestKeyName())
                                                           ? context().jsonKeyName()
                                                           : meta.requestKeyName();
        metadata.stream()
                .filter(Objects::nonNull)
                .map(meta -> new SimpleEntry<>(keyFunc.apply(meta),
                                               Functions.getIfThrow(() -> valFunc.apply(meta)).orElse(null)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .forEach(entry -> object.put(entry.getKey(), entry.getValue()));
        return object;
    }

}
