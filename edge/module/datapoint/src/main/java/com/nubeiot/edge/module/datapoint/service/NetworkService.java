package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.extension.EdgeExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class NetworkService extends AbstractReferencingEntityService<Network, NetworkMetadata>
    implements DataPointService<Network, NetworkMetadata> {

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
    public @NonNull RequestDecorator requestDecorator() {
        return EdgeExtension.create(this);
    }

    @Override
    public @NonNull EntityReferences referencedEntities() {
        return new EntityReferences().add(EdgeMetadata.INSTANCE, EdgeMetadata.INSTANCE.singularKeyName());
    }

    @Override
    public @NonNull Single<JsonObject> afterCreate(@NonNull Object key, @NonNull VertxPojo pojo,
                                                   @NonNull RequestData reqData) {
        final Set<String> ignoreFields = ignoreFields(reqData);
        ignoreFields.remove(EdgeMetadata.INSTANCE.singularKeyName());
        return Single.just(doTransform(EventAction.CREATE, key, pojo, reqData,
                                       (p, r) -> JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields)));
    }

}
