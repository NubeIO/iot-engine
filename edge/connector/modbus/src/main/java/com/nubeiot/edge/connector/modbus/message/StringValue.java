package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.json.Message;

import static com.nubeiot.edge.connector.modbus.message.Type.FLOAT_VALUE;

@AutoValue
public abstract class StringValue implements Message {
    public static StringValue of(String value) {
        return new AutoValue_StringValue(FLOAT_VALUE, value);
    }

    public abstract String value();

    public static TypeAdapter<StringValue> typeAdapter(Gson gson) {
        return new AutoValue_StringValue.GsonTypeAdapter(gson);
    }
}
