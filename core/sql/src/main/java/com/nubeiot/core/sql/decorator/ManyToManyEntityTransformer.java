package com.nubeiot.core.sql.decorator;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.ManyToManyResource;

import lombok.NonNull;

/**
 * Represents for Many to many entity transformer.
 *
 * @see ManyToManyResource
 * @see ReferenceEntityTransformer
 * @since 1.0.0
 */
public interface ManyToManyEntityTransformer extends ReferenceEntityTransformer, ManyToManyResource {

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Stream<String> manyStream = Stream.of(context().requestKeyName(), resource().requestKeyName());
        final Stream<String> referenceStream = references().stream().map(EntityMetadata::requestKeyName);
        return Stream.of(parent(requestData), refStream(requestData), manyStream, referenceStream).flatMap(s -> s).collect(Collectors.toSet());
    }

}
