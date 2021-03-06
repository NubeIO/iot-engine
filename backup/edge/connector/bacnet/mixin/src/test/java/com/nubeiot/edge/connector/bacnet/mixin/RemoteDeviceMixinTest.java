package com.nubeiot.edge.connector.bacnet.mixin;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class RemoteDeviceMixinTest {

    private ObjectIdentifier oid;
    private PropertyValuesMixin mixin;

    @Before
    public void setup() {
        oid = new ObjectIdentifier(ObjectType.device, 222);
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.groupId, null, new CharacterString("xxx"));
        pvs.add(oid, PropertyIdentifier.alarmValue, null, new Double(10.0));
        mixin = PropertyValuesMixin.create(oid, pvs, false);
    }

    @Test
    public void test_serialize_ip_mode() throws JSONException {
        final Address address = new Address(new byte[] {(byte) 206, (byte) 210, 100, (byte) 134});
        final RemoteDeviceMixin rdm = RemoteDeviceMixin.create(oid, "test", address, mixin);
        final JsonObject expected = new JsonObject(
            "{\"instanceNumber\":222,\"name\":\"test\",\"address\":{\"type\":\"IP\"," +
            "\"networkNumber\":0,\"hostAddress\":\"206.210.100.134\"," +
            "\"macAddress\":\"CE-D2-64-86\"},\"group-id\":\"xxx\",\"alarm-value\":10.0}");
        JsonHelper.assertJson(expected, rdm.toJson());
    }

    @Test
    public void test_serialize_mstp_mode() throws JSONException {
        final Address address = new Address(3, new byte[] {(byte) 12});
        final RemoteDeviceMixin rdm = RemoteDeviceMixin.create(oid, "hello", address, mixin);
        final JsonObject expected = new JsonObject(
            "{\"instanceNumber\":222,\"name\":\"hello\",\"address\":{\"type\":\"MSTP\",\"networkNumber\":3," +
            "\"hostAddress\":\"12\",\"macAddress\":\"0C\"},\"group-id\":\"xxx\",\"alarm-value\":10.0}");
        JsonHelper.assertJson(expected, rdm.toJson());
    }

}
