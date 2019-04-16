package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.json.Message;

import static com.nubeiot.edge.connector.modbus.message.Type.BOOLEAN_VALUE;

@AutoValue
public abstract class BooleanValue implements Message {
    public static BooleanValue of(boolean value) {
        return new AutoValue_BooleanValue(BOOLEAN_VALUE, value);
    }

    public abstract boolean value();

    public static TypeAdapter<BooleanValue> typeAdapter(Gson gson) {
        return new AutoValue_BooleanValue.GsonTypeAdapter(gson);
    }
}
