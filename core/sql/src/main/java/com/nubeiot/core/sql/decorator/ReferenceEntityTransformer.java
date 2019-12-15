package com.nubeiot.core.sql.decorator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.HasReferenceMarker;

import lombok.NonNull;

/**
 * Represents for Reference entity transformer.
 *
 * @see EntityTransformer
 * @since 1.0.0
 */
public interface ReferenceEntityTransformer extends EntityTransformer {

    /**
     * Declares {@code has reference} marker.
     *
     * @return the has reference resource
     * @see HasReferenceMarker
     * @since 1.0.0
     */
    HasReferenceMarker marker();

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(parent(requestData), refStream(requestData)).flatMap(s -> s).collect(Collectors.toSet());
    }

    /**
     * Parent stream.
     *
     * @param requestData the request data
     * @return the stream
     * @since 1.0.0
     */
    default Stream<String> parent(@NonNull RequestData requestData) {
        return EntityTransformer.super.ignoreFields(requestData).stream();
    }

    /**
     * Ref stream.
     *
     * @param requestData the request data
     * @return the stream
     * @since 1.0.0
     */
    default Stream<String> refStream(@NonNull RequestData requestData) {
        final JsonObject filter = Optional.ofNullable(requestData.filter()).orElseGet(JsonObject::new);
        return marker().ignoreFields()
                       .stream()
                       .filter(s -> filter.fieldNames().contains(s))
                       .filter(excludeResourceField());
    }

    /**
     * Exclude resource field predicate.
     *
     * @return the predicate
     * @since 1.0.0
     */
    default Predicate<String> excludeResourceField() {
        return s -> !resourceMetadata().jsonKeyName().equals(s) && !resourceMetadata().requestKeyName().equals(s);
    }

}
