package com.nubeiot.core.sql.decorator;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
        JsonObject filter = Optional.ofNullable(requestData.getFilter()).orElseGet(JsonObject::new);
        return Stream.of(EntityTransformer.super.ignoreFields(requestData).stream().filter(Objects::nonNull),
                         ref().ignoreFields().stream().filter(s -> filter.fieldNames().contains(s)))
                     .flatMap(s -> s).map(String::toLowerCase)
                     .collect(Collectors.toSet());
    }

}
