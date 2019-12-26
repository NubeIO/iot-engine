package com.nubeiot.core.sql.decorator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.marker.ManyToManyMarker;

import lombok.NonNull;

/**
 * Represents for Many to many entity transformer.
 *
 * @see ReferenceEntityTransformer
 * @since 1.0.0
 */
public interface ManyToManyEntityTransformer extends EntityTransformer {

    /**
     * Declares many to many marker.
     *
     * @return the many to many marker
     * @see ManyToManyMarker
     * @since 1.0.0
     */
    ManyToManyMarker marker();

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        return Stream.of(EntityTransformer.super.ignoreFields(requestData), marker().ignoreFields())
                     .flatMap(Collection::stream)
                     .map(String::toLowerCase)
                     .collect(Collectors.toSet());
    }

}
