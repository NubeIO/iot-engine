package com.nubeiot.edge.connector.bacnet.entity;

import org.json.JSONException;
import org.junit.Test;

import io.github.zero88.qwe.JsonHelper;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.entity.BACnetEntities.BACnetPoints;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class BACnetPointsTest {

    @Test
    public void test_serialize() throws JSONException {
        final String networkId = "ipv4-docker";
        final ObjectIdentifier deviceId = new ObjectIdentifier(ObjectType.device, 111);
        final ObjectIdentifier oid = new ObjectIdentifier(ObjectType.analogOutput, 1);
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.presentValue, null, new Double(10.0));
        pvs.add(oid, PropertyIdentifier.objectType, null, ObjectType.analogOutput);
        final PropertyValuesMixin pvm = PropertyValuesMixin.create(oid, pvs, false);
        final BACnetPointEntity pt = BACnetPointEntity.from(networkId, deviceId, pvm);
        JsonHelper.assertJson(new JsonObject("{\"analog-output:1\":{\"key\":\"analog-output:1\"," +
                                             "\"networkId\":\"ipv4-docker\",\"deviceId\":\"device:111\"," +
                                             "\"type\":\"ANALOG_OUTPUT\"}}"), new BACnetPoints().add(pt).toJson());
    }

}
