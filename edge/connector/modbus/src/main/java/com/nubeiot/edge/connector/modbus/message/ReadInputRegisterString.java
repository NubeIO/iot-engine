package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.nubeiot.edge.connector.modbus.ModbusVerticle;

@AutoValue
/**
 * Send an instance of this class to the {@link ModbusVerticle} to receive the value
 * of Input Registers in String format.
 */
public abstract class ReadInputRegisterString implements StringOperation {
    public static Builder builder() {
        return new AutoValue_ReadInputRegisterString.Builder()
                .setType(Type.READ_INPUT_REGISTER_STRING)
                .setByteCount(1);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setByteCount(int value);

        public abstract ReadInputRegisterString build();
    }

    public static TypeAdapter<ReadInputRegisterString> typeAdapter(Gson gson) {
        return new AutoValue_ReadInputRegisterString.GsonTypeAdapter(gson);
    }
}
