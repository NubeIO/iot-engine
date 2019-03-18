package com.nubeiot.edge.connector.bacnet.Util;

import java.util.ArrayList;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.obj.AnalogOutputObject;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.obj.BinaryOutputObject;
import com.serotonin.bacnet4j.obj.BinaryValueObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Double;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

/*
 * Static methods to create and add objects/points on the local BACnet device instance
 *
 * essentially a JSON parser to create local objects
 */
public class LocalPointObjectUtils {

    public static BACnetObject createLocalObject(JsonObject json, String pointID, LocalDevice localDevice) {
        BACnetObject obj;
        ArrayList<PropertyValue> proplist = new ArrayList<>();
        String name = "NAME_ERROR";
        float presentValue = 0;
        int inst;
        try {
            inst = Integer.parseInt(pointID.substring(pointID.length() - 1, pointID.length()));
        } catch (Exception e) {
            //TODO: handle virtual points
            return null;
        }
        //Have to get the name and present value first to create specific BACnet Object instance
        String[] jsonkeys = json.getMap().keySet().toArray(new String[json.size()]);
        for (int i = 0; i < jsonkeys.length; i++) {
            try {
                PropertyValue p = getProperyValue(jsonkeys[i], json.getValue(jsonkeys[i]));
                if (p == null) {
                    continue;
                }
                if (p.getPropertyIdentifier() == PropertyIdentifier.objectName) {
                    name = p.getValue().toString();
                } else if (p.getPropertyIdentifier() == PropertyIdentifier.presentValue) {
                    presentValue = ((Real) p.getValue()).floatValue();
                } else {
                    proplist.add(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String pointPrefix = pointID.substring(0, pointID.length() - 1);
        try {
            switch (pointPrefix) {
                case "UI":
                    obj = new AnalogInputObject(localDevice, inst, name, presentValue, EngineeringUnits.noUnits, false);
                    break;
                case "UO":
                    obj = new AnalogOutputObject(localDevice, inst, name, presentValue, EngineeringUnits.noUnits, false,
                                                 0);
                    break;
                case "DI":
                    obj = new BinaryInputObject(localDevice, inst, name, BinaryPV.active, false, Polarity.normal);
                    break;
                case "DO": {
                    BinaryPV binaryVal = presentValue == 0 ? BinaryPV.inactive : BinaryPV.active;
                    obj = new BinaryOutputObject(localDevice, inst, name, binaryVal, false, Polarity.normal,
                                                 BinaryPV.inactive);
                    break;
                }
                case "R": {
                    inst += 10;
                    BinaryPV binaryVal = presentValue == 0 ? BinaryPV.inactive : BinaryPV.active;
                    obj = new BinaryOutputObject(localDevice, inst, name, binaryVal, false, Polarity.normal,
                                                 BinaryPV.inactive);
                    break;
                }
                default: {
                    //TODO: what to do for virtual points?
                    return null;
                }
            }
        } catch (BACnetServiceException ex) {
            ex.printStackTrace();
            return null;
        }

        for (int i = 0; i < proplist.size(); i++) {
            PropertyValue p = proplist.get(i);
            obj.writePropertyInternal(p.getPropertyIdentifier(), p.getValue());
        }
        writePriorityArray(obj, json.getJsonObject("priorityArray"));
        return obj;
    }

    private static PropertyValue getProperyValue(String key, Object val) {
        switch (key) {
            case "name": {
                return new PropertyValue(PropertyIdentifier.objectName, new CharacterString((String) val));
            }
            case "value": {
                return new PropertyValue(PropertyIdentifier.presentValue, getValue(val));
            }
            //            case "tags": {
            //TODO: can't write tags , certain objects don't allow... does this matter?
            //                return new PropertyValue(PropertyIdentifier.tags, new BACnetArray<CharacterString>
            //                (getTags(val)));
            //            }
            //TODO: work out units converter/mapper
            default:
                return null;
        }
    }

    private static Real getValue(Object val) {
        try {
            float f = 0;
            if (val instanceof Integer) {
                f = new Float((Integer) val);
            } else if (val instanceof Double) {
                f = new Float((java.lang.Double) val);
            } else if (val instanceof Float) {
                f = (Float) val;
            }

            return new Real(f);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writePriorityArray(BACnetObject obj, JsonObject priorityArray) {
        if (priorityArray == null) {
            return;
        }
        Encodable val;
        for (int i = 1; i <= 16; i++) {
            Object o = priorityArray.getValue(Integer.toString(i));
            try {
                if (isBinary(obj) && !(o instanceof String)) {
                    val = BinaryPV.forId((Integer) o);
                } else if (o instanceof String) {
                    if (((String) o).equalsIgnoreCase("null")) {
                        continue;
                    } else {
                        val = new CharacterString((String) o);
                    }
                } else {
                    val = new Real(priorityArray.getFloat(Integer.toString(i)));
                }
                obj.writeProperty(new ValueSource(), new PropertyValue(PropertyIdentifier.presentValue, null, val,
                                                                       new UnsignedInteger(i)));
            } catch (Exception ex) {
                System.err.println(
                    "Object: " + obj.getInstanceId() + "  - Issue writing value " + o + " of type " + o.getClass() +
                    " @ priority " + i);
                System.out.println(
                    "Object: " + obj.getInstanceId() + "  - Issue writing value " + o + " of type " + o.getClass() +
                    " @ priority " + i);
                ex.printStackTrace();
            }
        }
    }

    private static CharacterString[] getTags(Object val) {
        JsonArray tagsTmp = ((JsonArray) val);
        CharacterString[] tags = new CharacterString[tagsTmp.size()];
        for (int i = 0; i < tagsTmp.size(); i++) {
            tags[i] = new CharacterString(tagsTmp.getString(i));
        }
        return tags;
    }

    private static boolean isBinary(BACnetObject obj) {
        return (obj instanceof BinaryInputObject || obj instanceof BinaryOutputObject ||
                obj instanceof BinaryValueObject);
    }

}
