package com.nubeiot.edge.connector.bacnet.mixin;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

public class ObjectPropertyValuesTest {

    @Test
    public void test_serialize_map() throws JSONException {
        final ObjectIdentifier oid = new ObjectIdentifier(ObjectType.device, 222);
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.groupId, null, new CharacterString("xxx"));
        pvs.add(oid, PropertyIdentifier.alarmValue, null, new Double(10.0));
        final PropertyValuesMixin mixin = PropertyValuesMixin.create(oid, pvs, false);
        final ObjectPropertyValues opvs = new ObjectPropertyValues().add(oid, mixin);
        JsonHelper.assertJson(new JsonObject("{\"device:222\":{\"group-id\":\"xxx\",\"alarm-value\":10.0}}"),
                              opvs.toJson());
    }

}
