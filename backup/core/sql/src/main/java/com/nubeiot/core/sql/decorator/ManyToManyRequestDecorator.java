package com.nubeiot.core.sql.decorator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.ManyToManyMarker;

import lombok.NonNull;

public interface ManyToManyRequestDecorator extends HasReferenceRequestDecorator {

    ManyToManyMarker marker();

    @Override
    @NonNull
    default RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, HasReferenceRequestDecorator.convertKey(requestData, context(),
                                                                                         marker().references()));
    }

    @Override
    @NonNull
    default RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final List<EntityMetadata> list = Stream.concat(marker().references().stream(), Stream.of(marker().resource()))
                                                .collect(Collectors.toList());
        return recomputeRequestData(requestData,
                                    HasReferenceRequestDecorator.convertKey(requestData, marker().context(), list));
    }

    @Override
    @NonNull
    default RequestData onDeletingOneResource(@NonNull RequestData requestData) {
        return onModifyingOneResource(requestData);
    }

    @Override
    @NonNull
    default RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return onCreatingOneResource(requestData);
    }

    @Override
    default @NonNull RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return onModifyingOneResource(requestData);
    }

}
