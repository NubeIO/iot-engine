package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.json.Message;

import static com.nubeiot.edge.connector.modbus.message.Type.FLOAT_VALUE;

@AutoValue
public abstract class FloatValue implements Message {
    public static FloatValue of(double value) {
        return new AutoValue_FloatValue(FLOAT_VALUE, value);
    }

    public abstract double value();

    public static TypeAdapter<FloatValue> typeAdapter(Gson gson) {
        return new AutoValue_FloatValue.GsonTypeAdapter(gson);
    }
}
