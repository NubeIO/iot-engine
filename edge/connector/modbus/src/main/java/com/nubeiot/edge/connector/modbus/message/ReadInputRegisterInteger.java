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
public abstract class ReadInputRegisterInteger implements IntegerOperation {

    public static Builder builder() {
        return new AutoValue_ReadInputRegisterInteger.Builder()
                .setType(Type.READ_INPUT_REGISTER_INTEGER)
                .setUnsigned(false)
                .setByteCount(2)
                .setSwapBytes(false)
                .setSwapRegisters(false);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setUnsigned(boolean value);

        public abstract Builder setByteCount(int value);

        public abstract Builder setSwapBytes(boolean value);

        public abstract Builder setSwapRegisters(boolean value);

        public abstract ReadInputRegisterInteger build();
    }

    public static TypeAdapter<ReadInputRegisterInteger> typeAdapter(Gson gson) {
        return new AutoValue_ReadInputRegisterInteger.GsonTypeAdapter(gson);
    }
}
