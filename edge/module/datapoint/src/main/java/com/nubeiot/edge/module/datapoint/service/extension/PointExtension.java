package com.nubeiot.edge.module.datapoint.service.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;

import lombok.NonNull;

public interface PointExtension extends ReferencingEntityMarker {

    static Set<EventMethodDefinition> oneToOneDefinitions(@NonNull Collection<EventAction> availableEvents,
                                                          @NonNull Supplier<String> servicePath,
                                                          @NonNull Supplier<String> requestKeySupplier) {
        final ActionMethodMapping mapping = ActionMethodMapping.by(ActionMethodMapping.CRD_MAP, availableEvents);
        final EventMethodDefinition definition = EventMethodDefinition.create(
            Urls.combinePath(EntityHttpService.toCapturePath(PointMetadata.INSTANCE), servicePath.get()), mapping);
        return Stream.of(Collections.singleton(definition),
                         EntityHttpService.createDefinitions(mapping, servicePath, requestKeySupplier))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    static RequestData createRequestData(@NonNull RequestData requestData, @NonNull JsonObject body) {
        final String pointId = requestData.body().getString(PointMetadata.INSTANCE.requestKeyName());
        return RequestData.builder()
                          .headers(requestData.headers())
                          .filter(requestData.filter())
                          .sort(requestData.sort())
                          .pagination(requestData.pagination())
                          .body(body.put(PointMetadata.INSTANCE.requestKeyName(), pointId))
                          .build();
    }

    @Override
    default EntityReferences referencedEntities() {
        return new EntityReferences().add(PointMetadata.INSTANCE, "point");
    }

}
