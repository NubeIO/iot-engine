package com.nubeiot.core.sql.decorator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.HasReferenceResource;

import lombok.NonNull;

public interface ReferenceEntityTransformer extends EntityTransformer {

    HasReferenceResource ref();

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(parent(requestData), refStream(requestData)).flatMap(s -> s).collect(Collectors.toSet());
    }

    default Stream<String> parent(@NonNull RequestData requestData) {
        return EntityTransformer.super.ignoreFields(requestData).stream();
    }

    default Stream<String> refStream(@NonNull RequestData requestData) {
        final JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        return ref().ignoreFields()
                    .stream()
                    .filter(s -> filter.fieldNames().contains(s))
                    .filter(excludeResourceField());
    }

    default Predicate<String> excludeResourceField() {
        return s -> !resourceMetadata().jsonKeyName().equals(s) && !resourceMetadata().requestKeyName().equals(s);
    }

}
