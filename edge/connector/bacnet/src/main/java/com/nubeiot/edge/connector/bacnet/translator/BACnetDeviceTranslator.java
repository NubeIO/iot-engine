package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;
import java.util.Optional;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.dto.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.dto.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierSerializer;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;
import com.serotonin.bacnet4j.type.enumerated.DeviceStatus;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

public final class BACnetDeviceTranslator implements BACnetTranslator<EdgeDeviceComposite, RemoteDeviceMixin>,
                                                     IoTEntityTranslator<EdgeDeviceComposite, RemoteDeviceMixin> {

    @Override
    public RemoteDeviceMixin from(EdgeDeviceComposite entity) {
        return null;
    }

    @Override
    public EdgeDeviceComposite to(RemoteDeviceMixin object) {
        final PropertyValuesMixin values = object.getPropertyValues();
        final String manufacturer = Strings.toString(values.getAndCast(PropertyIdentifier.vendorIdentifier)) + "-" +
                                    Strings.toString(values.getAndCast(PropertyIdentifier.vendorName));
        final DeviceType deviceType = getDeviceType(values);
        final State state = new BACnetStateTranslator().to((DeviceStatus) values.get(PropertyIdentifier.systemStatus));
        final Device device = new Device().setCode(ObjectIdentifierSerializer.serialize(object.getObjectId()))
                                          .setProtocol(protocol())
                                          .setType(deviceType)
                                          .setName(values.getAndCast(PropertyIdentifier.objectName))
                                          .setManufacturer(manufacturer)
                                          .setModel(values.getAndCast(PropertyIdentifier.modelName))
                                          .setVersion(values.getAndCast(PropertyIdentifier.applicationSoftwareVersion))
                                          .setState(state)
                                          .setMetadata(object.toJson());
        return new EdgeDeviceComposite().wrap(new EdgeDevice().setAddress(object.getAddress()))
                                        .put(DeviceMetadata.INSTANCE.requestKeyName(), device);
    }

    @Override
    public Class<EdgeDeviceComposite> fromType() {
        return EdgeDeviceComposite.class;
    }

    @Override
    public Class<RemoteDeviceMixin> toType() {
        return RemoteDeviceMixin.class;
    }

    private DeviceType getDeviceType(PropertyValuesMixin values) {
        return Optional.ofNullable(new BACnetDeviceTypeTranslator().to(values.get(PropertyIdentifier.deviceType)))
                       .orElse(Objects.isNull(values.getAndCast(PropertyIdentifier.deviceAddressBinding))
                               ? DeviceType.EQUIPMENT
                               : DeviceType.GATEWAY);
    }

}
