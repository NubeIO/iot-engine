package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.json.Message;

@AutoValue
public abstract class TypedMessage implements Message {
    public static TypeAdapter<TypedMessage> typeAdapter(Gson gson) {
        return new AutoValue_TypedMessage.GsonTypeAdapter(gson);
    }
}
