package com.nubeiot.core.sql.decorator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.GroupReferenceResource;

import lombok.NonNull;

public interface GroupEntityTransformer extends ReferenceEntityTransformer {

    GroupReferenceResource ref();

    /**
     * Ignore fields that includes {@code audit field}, {@code reference field} and {@code group field}
     *
     * @param requestData request data
     * @return ignore fields
     */
    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(ReferenceEntityTransformer.super.ignoreFields(requestData).stream(),
                         ref().ignoreFields().stream().filter(Objects::nonNull))
                     .flatMap(s -> s)
                     .map(String::toLowerCase)
                     .collect(Collectors.toSet());
    }

    /**
     * Same as {@link #ignoreFields(RequestData)} but without {@code group field}
     *
     * @param requestData request data
     * @return ignore fields
     */
    default Set<String> showGroupFields(@NonNull RequestData requestData) {
        return ReferenceEntityTransformer.super.ignoreFields(requestData);
    }

}
