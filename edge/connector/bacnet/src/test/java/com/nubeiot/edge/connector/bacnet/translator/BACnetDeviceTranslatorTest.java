package com.nubeiot.edge.connector.bacnet.translator;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.DeviceStatus;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyValues;

public class BACnetDeviceTranslatorTest {

    private ObjectIdentifier oid;
    private Address address;
    private JsonObject addressJson;
    private PropertyValuesMixin mixin;
    private RemoteDeviceMixin deviceMixin;
    private BACnetDeviceTranslator translator;

    @Before
    public void setup() {
        translator = new BACnetDeviceTranslator();
        oid = new ObjectIdentifier(ObjectType.device, 444);
        address = new Address(new byte[] {(byte) 206, (byte) 210, 100, (byte) 134});
        addressJson = new JsonObject(
            "{\"type\":\"IP\",\"networkNumber\":0,\"hostAddress\":\"206.210.100.134\",\"macAddress\":\"CE-D2-64-86\"}");
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.vendorIdentifier, null, new UnsignedInteger(LocalDeviceMetadata.VENDOR_ID));
        pvs.add(oid, PropertyIdentifier.vendorName, null, new CharacterString(LocalDeviceMetadata.VENDOR_NAME));
        mixin = PropertyValuesMixin.create(oid, pvs, false);
        deviceMixin = RemoteDeviceMixin.create(oid, "test", address, mixin);
    }

    @Test
    public void test_serialize() throws JSONException {
        final EdgeDeviceComposite composite = translator.serialize(deviceMixin);
        JsonHelper.assertJson(addressJson, composite.getAddress());
        final Device device = composite.getDevice();
        Assert.assertEquals("device:444", device.getCode());
        Assert.assertEquals(DeviceType.EQUIPMENT, device.getType());
        Assert.assertEquals(Protocol.BACNET, device.getProtocol());
        Assert.assertEquals(State.NONE, device.getState());
        Assert.assertEquals("1173-Nube iO Operations Pty Ltd", device.getManufacturer());
        JsonHelper.assertJson(deviceMixin.getPropertyValues().toJson(), device.getMetadata());
        JsonHelper.assertJson(deviceMixin.getAddress().toJson(), composite.getAddress());
    }

    @Test
    public void test_deserialize() throws JSONException {
        final Device device = new Device().setCode(ObjectIdentifierMixin.serialize(oid))
                                          .setManufacturer("1173-Nube iO Operations Pty Ltd")
                                          .setProtocol(Protocol.BACNET)
                                          .setState(State.ENABLED)
                                          .setType(DeviceType.EQUIPMENT);
        final EdgeDeviceComposite composite = (EdgeDeviceComposite) new EdgeDeviceComposite().setDevice(device)
                                                                                             .setAddress(addressJson);
        final RemoteDeviceMixin mixin = translator.deserialize(composite);
        Assert.assertNotNull(mixin);
        final PropertyValuesMixin propertyValues = mixin.getPropertyValues();
        JsonHelper.assertJson(mixin.getAddress().toJson(), addressJson);
        Assert.assertNull(mixin.getName());
        Assert.assertEquals(oid, mixin.getObjectId());
        Assert.assertEquals(444, mixin.getInstanceNumber());
        Assert.assertNotNull(propertyValues);
        Assert.assertEquals(1173, (long) propertyValues.getAndCast(PropertyIdentifier.vendorIdentifier));
        Assert.assertEquals("Nube iO Operations Pty Ltd", propertyValues.getAndCast(PropertyIdentifier.vendorName));
        Assert.assertEquals(DeviceStatus.operational, propertyValues.get(PropertyIdentifier.systemStatus));
    }

}
