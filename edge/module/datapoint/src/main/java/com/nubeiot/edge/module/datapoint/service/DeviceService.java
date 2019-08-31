package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.EntityTransformer;
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

    @Override
    public Single<JsonObject> cudResponse(@NonNull String keyName, @NonNull Object key,
                                          @NonNull Function<Object, Single<JsonObject>> provider) {
        return provider.apply(key)
                       .doOnSuccess(this::addSyncConfig)
                       .map(device -> enableFullResourceInCUDResponse()
                                      ? device
                                      : EntityTransformer.keyResponse(keyName, key));
    }

    private void addSyncConfig(JsonObject device) {
        final JsonObject syncConfig = EntityTransformer.getData(device)
                                                       .getJsonObject("metadata", new JsonObject())
                                                       .getJsonObject(DataSyncConfig.NAME, new JsonObject());
        entityHandler().sharedData(DataPointIndex.DATA_SYNC_CFG, syncConfig);
    }

    public interface DeviceExtension extends HasReferenceResource {

        @Override
        default EntityReferences entityReferences() {
            return new EntityReferences().add(DeviceMetadata.INSTANCE, "device");
        }

    }

}
