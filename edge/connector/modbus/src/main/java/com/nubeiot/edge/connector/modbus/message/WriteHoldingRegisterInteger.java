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
public abstract class WriteHoldingRegisterInteger implements IntegerOperation, WriteOperation<Long> {
    public abstract Long value();

    public static Builder builder() {
        return new AutoValue_WriteHoldingRegisterInteger.Builder()
                .setType(Type.WRITE_HOLDING_REGISTER_INTEGER)
                .setUnsigned(false)
                .setByteCount(2)
                .setSwapBytes(false)
                .setSwapRegisters(false);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setValue(Long value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setUnsigned(boolean value);

        public abstract Builder setByteCount(int value);

        public abstract Builder setSwapBytes(boolean value);

        public abstract Builder setSwapRegisters(boolean value);

        public abstract WriteHoldingRegisterInteger build();
    }

    public static TypeAdapter<WriteHoldingRegisterInteger> typeAdapter(Gson gson) {
        return new AutoValue_WriteHoldingRegisterInteger.GsonTypeAdapter(gson);
    }
}
