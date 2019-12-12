package com.nubeiot.core.sql.decorator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.GroupReferenceResource;

import lombok.NonNull;

/**
 * The interface Group entity transformer.
 *
 * @see ReferenceEntityTransformer
 * @since 1.0.0
 */
public interface GroupEntityTransformer extends ReferenceEntityTransformer {

    /**
     * @see GroupReferenceResource
     */
    GroupReferenceResource ref();

    /**
     * Ignore fields that includes {@code audit field}, {@code reference field} and {@code group field}
     *
     * @param requestData request data
     * @return ignore fields
     */
    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Stream<String> groupStream = ref().ignoreFields()
                                                .stream()
                                                .filter(Objects::nonNull)
                                                .filter(excludeResourceField());
        return Stream.of(ReferenceEntityTransformer.super.ignoreFields(requestData).stream(), groupStream)
                     .flatMap(s -> s)
                     .map(String::toLowerCase)
                     .collect(Collectors.toSet());
    }

    /**
     * Same as {@link #ignoreFields(RequestData)} but without {@code group field}
     *
     * @param requestData request data
     * @return ignore fields
     * @since 1.0.0
     */
    default Set<String> showGroupFields(@NonNull RequestData requestData) {
        return ReferenceEntityTransformer.super.ignoreFields(requestData);
    }

}
