package com.nubeiot.core.sql.decorator;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.ManyToManyResource;

import lombok.NonNull;

public interface ManyToManyEntityTransformer extends ReferenceEntityTransformer, ManyToManyResource {

    @Override
    default Set<String> ignoreFields(@NonNull RequestData requestData) {
        final Stream<String> manyStream = Stream.of(context().requestKeyName(), reference().requestKeyName(),
                                                    resource().requestKeyName());
        return Stream.of(parent(requestData), refStream(requestData), manyStream)
                     .flatMap(s -> s)
                     .collect(Collectors.toSet());
    }

}
