package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.ModbusVerticle;

@AutoValue
/**
 * Send an instance of this class to the {@link ModbusVerticle} to receive the value
 * of an Input Register.
 */
public abstract class ReadInputContact implements ValueOperation {
    public static ReadInputContact.Builder builder() {
        return new AutoValue_ReadInputContact.Builder().setType(Type.READ_INPUT_CONTACT);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract ReadInputContact build();
    }

    public static TypeAdapter<ReadInputContact> typeAdapter(Gson gson) {
        return new AutoValue_ReadInputContact.GsonTypeAdapter(gson);
    }
}
