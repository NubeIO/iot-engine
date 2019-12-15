package com.nubeiot.edge.module.datapoint.service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

import lombok.NonNull;

public final class DeviceService extends AbstractEntityService<Device, DeviceMetadata>
    implements DataPointService<Device, DeviceMetadata> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public DeviceMetadata context() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.PATCH);
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        RequestData reqData = onModifyingOneResource(requestData);
        return doPatch(reqData).flatMap(pk -> doLookupByPrimaryKey(pk).map(pojo -> new SimpleEntry<>(pk, pojo)))
                               .map(this::cacheConfig)
                               .doOnSuccess(
                                   j -> asyncPostService().onSuccess(this, EventAction.PATCH, j.getValue(), reqData))
                               .doOnError(t -> asyncPostService().onError(this, EventAction.PATCH, t))
                               .flatMap(resp -> transformer().afterPatch(resp.getKey(), resp.getValue(), reqData));
    }

    private Entry<?, ? extends VertxPojo> cacheConfig(Entry<?, ? extends VertxPojo> entry) {
        final JsonObject syncConfig = Optional.ofNullable(((Device) entry.getValue()).getMetadata())
                                              .map(info -> info.getJsonObject(DataSyncConfig.NAME, new JsonObject()))
                                              .orElse(new JsonObject());
        entityHandler().addSharedData(DataPointIndex.DATA_SYNC_CFG, syncConfig);
        return entry;
    }

    public interface DeviceExtension extends HasReferenceResource {

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(DeviceMetadata.INSTANCE, "device");
        }

    }

}
