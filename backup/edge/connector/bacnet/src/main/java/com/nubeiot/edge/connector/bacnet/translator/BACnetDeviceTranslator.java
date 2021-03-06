package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.sql.type.Label;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;
import com.serotonin.bacnet4j.type.enumerated.DeviceStatus;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class BACnetDeviceTranslator implements BACnetTranslator<EdgeDeviceComposite, RemoteDeviceMixin>,
                                                     IoTEntityTranslator<EdgeDeviceComposite, RemoteDeviceMixin> {

    @Override
    public EdgeDeviceComposite serialize(RemoteDeviceMixin remoteDevice) {
        if (Objects.isNull(remoteDevice)) {
            throw new IllegalArgumentException("Remote device is invalid. Cannot convert to persistence data");
        }
        final PropertyValuesMixin values = remoteDevice.getPropertyValues();
        final String manufacturer = String.join("-", Arrays.asList(
            Strings.toString(values.encode(PropertyIdentifier.vendorIdentifier)),
            Strings.toString(values.encode(PropertyIdentifier.vendorName))));
        final DeviceType deviceType = analyzeDeviceType(values);
        final State state = new BACnetStateTranslator().serialize(
            (DeviceStatus) values.getAndCast(PropertyIdentifier.systemStatus).orElse(DeviceStatus.nonOperational));
        final Label label = Label.builder()
                                 .description(values.encode(PropertyIdentifier.description))
                                 .label(Optional.ofNullable((String) values.encode(PropertyIdentifier.objectName))
                                                .orElse(remoteDevice.getName()))
                                 .build();
        final Device device = new Device().setCode(ObjectIdentifierMixin.serialize(remoteDevice.getObjectId()))
                                          .setType(deviceType)
                                          .setProtocol(protocol())
                                          .setName(remoteDevice.getName())
                                          .setManufacturer(manufacturer)
                                          .setModel(values.encode(PropertyIdentifier.modelName))
                                          .setFirmwareVersion(values.encode(PropertyIdentifier.firmwareRevision))
                                          .setSoftwareVersion(
                                              values.encode(PropertyIdentifier.applicationSoftwareVersion))
                                          .setState(state)
                                          .setLabel(label)
                                          .setMetadata(remoteDevice.getPropertyValues().toJson());
        final EdgeDevice pojo = new EdgeDevice().setAddress(remoteDevice.getAddress().toJson())
                                                .setNetworkId(remoteDevice.getNetworkId());
        return new EdgeDeviceComposite().wrap(pojo).setDevice(device);
    }

    @Override
    public RemoteDeviceMixin deserialize(EdgeDeviceComposite entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        final Device device = entity.getOther(DeviceMetadata.INSTANCE.singularKeyName());
        if (Objects.isNull(device)) {
            return null;
        }
        if (!protocol().equals(device.getProtocol())) {
            throw new IllegalArgumentException("Invalid BACnet device");
        }
        final JsonObject properties = Optional.ofNullable(device.getMetadata()).orElse(new JsonObject());
        final ObjectIdentifier identifier = Functions.getOrDefault(
            () -> ObjectIdentifierMixin.deserialize(device.getCode()), () -> ObjectIdentifierMixin.deserialize(
                properties.getString(PropertyIdentifier.objectIdentifier.toString())));
        final String name = Optional.ofNullable(device.getName())
                                    .orElse(properties.getString(PropertyIdentifier.objectName.toString()));
        final JsonObject props = toProperties(device, identifier);
        return RemoteDeviceMixin.create(identifier, name, entity.getAddress(), properties.mergeIn(props, true))
                                .setNetworkId(entity.getNetworkId());
    }

    @Override
    public Class<EdgeDeviceComposite> fromType() {
        return EdgeDeviceComposite.class;
    }

    @Override
    public Class<RemoteDeviceMixin> toType() {
        return RemoteDeviceMixin.class;
    }

    private DeviceType analyzeDeviceType(@NonNull PropertyValuesMixin values) {
        return Objects.isNull(values.encode(PropertyIdentifier.deviceAddressBinding))
               ? DeviceType.EQUIPMENT
               : DeviceType.GATEWAY;
    }

    private JsonObject toProperties(@NonNull Device device, @NonNull ObjectIdentifier identifier) {
        final String vendor = device.getManufacturer();
        final Integer vendorId = Functions.getOrDefault((Integer) null,
                                                        () -> Functions.toInt().apply(vendor.split("-", 2)[0]));
        final String vendorName = Functions.getOrDefault((String) null, () -> vendor.split("-", 2)[1]);
        final Map<PropertyIdentifier, Object> values = new HashMap<>();
        values.put(PropertyIdentifier.objectIdentifier, identifier);
        values.put(PropertyIdentifier.vendorIdentifier, vendorId);
        values.put(PropertyIdentifier.vendorName, vendorName);
        values.put(PropertyIdentifier.modelName, device.getModel());
        values.put(PropertyIdentifier.firmwareRevision, device.getFirmwareVersion());
        values.put(PropertyIdentifier.applicationSoftwareVersion, device.getSoftwareVersion());
        values.put(PropertyIdentifier.systemStatus, new BACnetStateTranslator().deserialize(device.getState()));
        values.put(PropertyIdentifier.description,
                   Optional.ofNullable(device.getLabel()).map(Label::getDescription).orElse(null));
        return BACnetMixin.MAPPER.convertValue(values, JsonObject.class);
    }

}
