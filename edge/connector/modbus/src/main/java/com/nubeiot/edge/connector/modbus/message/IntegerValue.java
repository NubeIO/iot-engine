package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.json.Message;

import static com.nubeiot.edge.connector.modbus.message.Type.INTEGER_VALUE;

@AutoValue
public abstract class IntegerValue implements Message {
    public static IntegerValue of(long value) {
        return new AutoValue_IntegerValue(INTEGER_VALUE, value);
    }

    public abstract long value();

    public static TypeAdapter<IntegerValue> typeAdapter(Gson gson) {
        return new AutoValue_IntegerValue.GsonTypeAdapter(gson);
    }
}
