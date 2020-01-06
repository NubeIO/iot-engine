package com.nubeiot.edge.module.datapoint.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.EdgeService.EdgeExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractReferencingEntityService<Network, NetworkMetadata>
    implements DataPointService<Network, NetworkMetadata>, EdgeExtension {

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public NetworkMetadata context() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             EntityHttpService.createDefinitions(getAvailableEvents(), context(), EdgeMetadata.INSTANCE)
                                              .stream()).collect(Collectors.toSet());
    }

    @Override
    protected RequestData recomputeRequestData(@NonNull RequestData requestData, JsonObject extra) {
        final com.nubeiot.iotdata.edge.model.tables.@NonNull Network table = context().table();
        EdgeExtension.optimizeReqData(entityHandler(), requestData, table.getJsonField(table.EDGE));
        return super.recomputeRequestData(requestData, extra);
    }

    public interface NetworkExtension extends ReferencingEntityMarker {

        static void optimizeAlias(@NonNull EntityHandler handler, @NonNull RequestData requestData) {
            optimizeAlias(handler, requestData, null);
        }

        static void optimizeAlias(@NonNull EntityHandler handler, @NonNull RequestData requestData,
                                  String networkField) {
            final String networkId = handler.sharedData(DataPointIndex.DEFAULT_NETWORK_ID);
            final String networkKey = Strings.fallback(networkField, NetworkMetadata.INSTANCE.requestKeyName());
            optimizeAlias(requestData.body(), networkId, networkKey);
            optimizeAlias(requestData.filter(), networkId, networkKey);
        }

        static void optimizeAlias(JsonObject req, @NonNull String networkId, @NonNull String networkKey) {
            Optional.ofNullable(req).ifPresent(r -> {
                if (NetworkMetadata.DEFAULT_ALIASES.contains(r.getString(networkKey, "").toUpperCase())) {
                    r.put(networkKey, networkId);
                }
            });
        }

        @Override
        default EntityReferences referencedEntities() {
            return new EntityReferences().add(NetworkMetadata.INSTANCE, "network");
        }

    }

}
