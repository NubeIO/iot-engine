package com.nubeiot.edge.connector.bacnet.discovery;

import java.util.Collections;

import org.json.JSONException;
import org.junit.Test;

import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.protocol.network.Ipv4Network;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntities.BACnetPoints;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class DiscoveryResponseTest {

    @Test
    public void test_serialize() throws JSONException {
        final BACnetConfig config = BACnetConfig.builder().deviceId(111).modelName("xyz").build();
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final ObjectIdentifier oid = new ObjectIdentifier(ObjectType.analogInput, 1);
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.presentValue, null, new Double(10.0));
        pvs.add(oid, PropertyIdentifier.deviceType, null, new CharacterString("abc"));
        final PropertyValuesMixin pvm = PropertyValuesMixin.create(oid, pvs, false);
        final Address address = new Address(new byte[] {(byte) 206, (byte) 210, 100, (byte) 134});
        final BACnetDeviceEntity dt = BACnetDeviceEntity.from(firstActiveIp.identifier(),
                                                              RemoteDeviceMixin.create(oid, address, pvm));
        final BACnetPointEntity pt = BACnetPointEntity.from(firstActiveIp.identifier(), oid, pvm);
        final BACnetPoints opv = (BACnetPoints) new BACnetPoints().add(pt);
        final DiscoveryResponse response = DiscoveryResponse.builder()
                                                            .network(firstActiveIp)
                                                            .config(config)
                                                            .remoteDevice(dt)
                                                            .remoteDevices(Collections.singletonList(dt))
                                                            .objects(opv)
                                                            .object(pt)
                                                            .build();
        final JsonObject expected = new JsonObject().put("network", firstActiveIp.toJson())
                                                    .put("config", config.toJson())
                                                    .put("objects", opv.toJson())
                                                    .put("object", pt.toJson())
                                                    .put("remoteDevices", new JsonArray().add(dt.toJson()))
                                                    .put("remoteDevice", dt.toJson());
        System.out.println(response.toJson().encodePrettily());
        JsonHelper.assertJson(expected, response.toJson());
    }

    @Test
    public void test_deserialize() throws JSONException {
        final Ipv4Network firstActiveIp = Ipv4Network.getFirstActiveIp();
        final BACnetConfig config = BACnetConfig.builder().deviceId(222).modelName("abc").build();
        final JsonObject expected = new JsonObject().put("network", firstActiveIp.toJson())
                                                    .put("config", config.toJson());
        final DiscoveryResponse xyz = JsonData.from(expected, DiscoveryResponse.class);
        System.out.println(xyz.toJson());
        JsonHelper.assertJson(expected, xyz.toJson());
    }

}
