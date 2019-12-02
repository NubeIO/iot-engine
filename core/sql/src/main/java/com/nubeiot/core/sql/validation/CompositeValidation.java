package com.nubeiot.core.sql.validation;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Entity validation for request input
 *
 * @param <P> Vertx pojo type
 * @param <C> Composite pojo that extends from Vertx pojo type
 */
@SuppressWarnings("unchecked")
public interface CompositeValidation<P extends VertxPojo, C extends CompositePojo<P, C>> extends EntityValidation<P> {

    @Override
    CompositeMetadata context();

    @Override
    @NonNull
    default C onCreating(RequestData reqData) throws IllegalArgumentException {
        final C request = (C) context().parseFromRequest(reqData.body());
        for (EntityMetadata metadata : ((List<EntityMetadata>) context().subItems())) {
            String key = metadata.singularKeyName();
            final VertxPojo sub = request.getOther(key);
            if (Objects.isNull(sub)) {
                continue;
            }
            request.put(key, Functions.getOrThrow(() -> metadata.onCreating(RequestData.builder().body(reqData.body().getJsonObject(key)).build()),
                                                  NubeExceptionConverter::from));
        }
        return request;
    }

    @Override
    default <PP extends P> @NonNull PP onUpdating(@NonNull P dbData, RequestData reqData)
        throws IllegalArgumentException {
        C request = (C) context().parseFromRequest(reqData.body());
        validateSubItems((C) dbData, request, (metadata, requestData, pojo) -> metadata.onUpdating(pojo, requestData));
        return (PP) request;
    }

    @Override
    default <PP extends P> @NonNull PP onPatching(@NonNull P dbData, RequestData reqData)
        throws IllegalArgumentException {
        C request = (C) context().parseFromEntity(JsonPojo.merge(dbData, context().parseFromRequest(reqData.body())));
        validateSubItems((C) dbData, request, (metadata, requestData, pojo) -> metadata.onPatching(pojo, requestData));
        return (PP) request;
    }

    @Override
    default <PP extends P> PP onDeleting(P dbData, @NonNull RequestData reqData) throws IllegalArgumentException {
        return (PP) dbData;
    }

    default String msg(@NonNull JsonObject data, @NonNull Collection<EntityMetadata> references) {
        return references.stream()
                         .filter(Objects::nonNull)
                         .filter(m -> !context().requestKeyName().equals(m.requestKeyName()))
                         .map(ref -> new SimpleEntry<>(ref.requestKeyName(), data.getValue(ref.requestKeyName())))
                         .map(Strings.kvMsg())
                         .collect(Collectors.joining(" and "));
    }

    default void validateSubItems(@NonNull C dbData, @NonNull C request,
                                  Function3<EntityMetadata, RequestData, VertxPojo, VertxPojo> validator) {
        for (EntityMetadata metadata : ((List<EntityMetadata>) context().subItems())) {
            String key = metadata.singularKeyName();
            final VertxPojo sub = request.getOther(key);
            if (Objects.isNull(sub)) {
                continue;
            }
            try {
                validator.apply(metadata, RequestData.builder().body(sub.toJson()).build(),
                                dbData.safeGetOther(key, metadata.modelClass()));
            } catch (Exception e) {
                throw NubeExceptionConverter.from(e);
            }
        }
    }

}
