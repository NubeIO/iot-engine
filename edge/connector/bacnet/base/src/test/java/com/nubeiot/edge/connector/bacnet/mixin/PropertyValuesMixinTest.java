package com.nubeiot.edge.connector.bacnet.mixin;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.serotonin.bacnet4j.enums.DayOfWeek;
import com.serotonin.bacnet4j.enums.Month;
import com.serotonin.bacnet4j.type.constructed.AssignedLandingCalls.LandingCall;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.NameValue;
import com.serotonin.bacnet4j.type.constructed.PriorityValue;
import com.serotonin.bacnet4j.type.constructed.Recipient;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.constructed.TimerStateChangeValue;
import com.serotonin.bacnet4j.type.enumerated.Action;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.LiftCarDirection;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.SignedInteger;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.Unsigned8;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyValues;

public class PropertyValuesMixinTest {

    private final ObjectIdentifier oid = new ObjectIdentifier(ObjectType.device, 111);

    @Test
    public void test_serialize_java_type_or_primitive() throws JSONException {
        final PropertyValues pvs = new PropertyValues();
        final Date date = new Date(2019, Month.DECEMBER, 22, DayOfWeek.SUNDAY);
        final Time time = new Time(18, 36, 56, 999);
        pvs.add(oid, PropertyIdentifier.objectIdentifier, null, oid);
        pvs.add(oid, PropertyIdentifier.groupId, null, new CharacterString("xxx"));
        pvs.add(oid, PropertyIdentifier.timeOfDeviceRestart, null, date);
        pvs.add(oid, PropertyIdentifier.activationTime, null, time);
        pvs.add(oid, PropertyIdentifier.expirationTime, null, new DateTime(date, time));
        pvs.add(oid, PropertyIdentifier.isUtc, null, Boolean.valueOf(true));
        pvs.add(oid, PropertyIdentifier.alarmValue, null, new Double(10.0));
        pvs.add(oid, PropertyIdentifier.adjustValue, null, new Real(20.5f));
        pvs.add(oid, PropertyIdentifier.averageValue, null, new SignedInteger(-100));
        pvs.add(oid, PropertyIdentifier.feedbackValue, null, new UnsignedInteger(100));
        pvs.add(oid, PropertyIdentifier.statusFlags, null, new StatusFlags(true, false, true, false));
        PropertyValuesMixin pvJson = new PropertyValuesMixin(pvs, false);
        final JsonObject expected = new JsonObject(
            "{\"status-flags\":{\"in-alarm\":true,\"fault\":false,\"overridden\":true,\"out-of-service\":false}," +
            "\"adjust-value\":20.5,\"group-id\":\"xxx\",\"alarm-value\":10.0,\"is-utc\":true,\"feedback-value\":100," +
            "\"object-identifier\":\"device:111\",\"time-of-device-restart\":\"2019-12-22T05:00:00Z\"," +
            "\"average-value\":-100,\"activation-time\":\"18:36:56.000000999Z\"," +
            "\"expiration-time\":\"2019-12-22T11:37:05.99Z\"}");
        System.out.println(pvJson.toJson().encode());
        JsonHelper.assertJson(expected, pvJson.toJson());
    }

    @Test
    public void test_serialize_enumerated() throws JSONException {
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.action, null, Action.direct);
        pvs.add(oid, PropertyIdentifier.errorLimit, null,
                new ErrorClassAndCode(ErrorClass.device, ErrorCode.abortSecurityError));
        PropertyValuesMixin pvJson = new PropertyValuesMixin(pvs, true);
        final JsonObject expected = new JsonObject("{\"action\":\"direct\",\"error-limit\":{\"errorClass\":\"device" +
                                                   "\",\"errorCode\":\"abort-security-error\"}}");
        System.out.println(pvJson.toJson().encode());
        JsonHelper.assertJson(expected, pvJson.toJson());
    }

    @Test
    public void test_serialize_baseType() throws JSONException {
        final PropertyValues pvs = new PropertyValues();
        pvs.add(oid, PropertyIdentifier.activeText, null, new NameValue("abc", new CharacterString("xxx")));
        pvs.add(oid, PropertyIdentifier.landingCalls, null, new LandingCall(new Unsigned8(12), LiftCarDirection.up));
        pvs.add(oid, PropertyIdentifier.priorityForWriting, null, new PriorityValue(new Unsigned8(16)));
        pvs.add(oid, PropertyIdentifier.timerState, null, new TimerStateChangeValue(Boolean.TRUE));
        pvs.add(oid, PropertyIdentifier.restartNotificationRecipients, null,
                new Recipient(new ObjectIdentifier(ObjectType.device, 222)));
        PropertyValuesMixin pvJson = new PropertyValuesMixin(pvs, true);
        final JsonObject expected = new JsonObject("{\"active-text\":{\"name\":\"abc\",\"value\":\"xxx\"}," +
                                                   "\"landing-calls\":{\"floor-number\":12,\"direction\":\"up\"}," +
                                                   "\"priority-for-writing\":16," +
                                                   "\"restart-notification-recipients\":\"device:222\"," +
                                                   "\"timer-state\":{\"choice\":true}}");
        System.out.println(pvJson.toJson().encode());
        JsonHelper.assertJson(expected, pvJson.toJson());
    }

}
