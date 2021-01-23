package com.nubeiot.edge.connector.bacnet.converter;

public class BACnetDeviceTranslatorTest {

    //    private ObjectIdentifier oid;
    //    private JsonObject addressJson;
    //    private RemoteDeviceMixin deviceMixin;
    //
    //    @Before
    //    public void setup() {
    //        oid = new ObjectIdentifier(ObjectType.device, 444);
    //        final Address address = new Address(new byte[] {(byte) 206, (byte) 210, 100, (byte) 134});
    //        addressJson = new JsonObject(
    //            "{\"type\":\"IP\",\"networkNumber\":0,\"hostAddress\":\"206.210.100.134\",
    //            \"macAddress\":\"CE-D2-64-86\"}");
    //        final PropertyValues pvs = new PropertyValues();
    //        pvs.add(oid, PropertyIdentifier.vendorIdentifier, null, new UnsignedInteger(LocalDeviceMetadata
    //        .VENDOR_ID));
    //        pvs.add(oid, PropertyIdentifier.vendorName, null, new CharacterString(LocalDeviceMetadata.VENDOR_NAME));
    //        pvs.add(oid, PropertyIdentifier.systemStatus, null, DeviceStatus.operational);
    //        final PropertyValuesMixin mixin = PropertyValuesMixin.create(oid, pvs, false);
    //        deviceMixin = RemoteDeviceMixin.create(oid, "test", address, mixin);
    //    }
    //
    //    @Test
    //    public void test_serialize() throws JSONException {
    //        final EdgeDeviceComposite composite = new BACnetDeviceConverter().serialize(deviceMixin);
    //        JsonHelper.assertJson(addressJson, composite.getAddress());
    //        final Device device = composite.getDevice();
    //        Assert.assertEquals("device:444", device.getCode());
    //        Assert.assertEquals(DeviceType.EQUIPMENT, device.getType());
    //        Assert.assertEquals(Protocol.BACNET, device.getProtocol());
    //        Assert.assertEquals(State.ENABLED, device.getState());
    //        Assert.assertEquals("1173-Nube iO Operations Pty Ltd", device.getManufacturer());
    //        JsonHelper.assertJson(deviceMixin.getPropertyValues().toJson(), device.getMetadata());
    //        JsonHelper.assertJson(deviceMixin.getAddress().toJson(), composite.getAddress());
    //    }
    //
    //    @Test
    //    public void test_deserialize_without_metadata() throws JSONException {
    //        final Device device = new Device().setCode(ObjectIdentifierMixin.serialize(oid))
    //                                          .setManufacturer("1173-Nube iO Operations Pty Ltd")
    //                                          .setProtocol(Protocol.BACNET)
    //                                          .setState(State.ENABLED)
    //                                          .setType(DeviceType.EQUIPMENT);
    //        final EdgeDeviceComposite composite = (EdgeDeviceComposite) new EdgeDeviceComposite().setDevice(device)
    //                                                                                             .setAddress
    //                                                                                             (addressJson);
    //        final RemoteDeviceMixin mixin = new BACnetDeviceConverter().deserialize(composite);
    //        Assert.assertNotNull(mixin);
    //        System.out.println(mixin.toJson());
    //        final PropertyValuesMixin propertyValues = mixin.getPropertyValues();
    //        JsonHelper.assertJson(mixin.getAddress().toJson(), addressJson);
    //        Assert.assertNull(mixin.getName());
    //        Assert.assertEquals(oid, mixin.getObjectId());
    //        Assert.assertEquals(444, mixin.getInstanceNumber());
    //        Assert.assertNotNull(propertyValues);
    //        Assert.assertEquals(1173, (long) propertyValues.encode(PropertyIdentifier.vendorIdentifier));
    //        Assert.assertEquals("Nube iO Operations Pty Ltd", propertyValues.encode(PropertyIdentifier.vendorName));
    //        Assert.assertEquals(DeviceStatus.operational, propertyValues.get(PropertyIdentifier.systemStatus));
    //    }
}
