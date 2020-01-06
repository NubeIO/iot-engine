package com.nubeiot.edge.module.datapoint.model.pojos;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class EdgeDeviceComposite extends EdgeDevice implements CompositePojo<EdgeDevice, EdgeDeviceComposite> {

    @Getter
    private final ExtensionPojo extension = new ExtensionPojo();

    @Override
    public EdgeDeviceComposite wrap(@NonNull EdgeDevice pojo) {
        this.from(pojo);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(extensionToJson(), true);
    }

    public EdgeDeviceComposite addDevice(Device device) {
        this.put(DeviceMetadata.INSTANCE.singularKeyName(), device);
        return this;
    }

    public Device getDevice() {
        return this.getOther(DeviceMetadata.INSTANCE.singularKeyName());
    }

}
