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
public abstract class WriteHoldingRegisterFloat implements FloatOperation, WriteOperation<Double> {
    public abstract Double value();

    public static Builder builder() {
        return new AutoValue_WriteHoldingRegisterFloat.Builder()
                .setType(Type.WRITE_HOLDING_REGISTER_FLOAT)
                .setByteCount(4)
                .setSwapRegisters(false);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setValue(Double value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setByteCount(int value);

        public abstract Builder setSwapRegisters(boolean value);

        public abstract WriteHoldingRegisterFloat build();
    }

    public static TypeAdapter<WriteHoldingRegisterFloat> typeAdapter(Gson gson) {
        return new AutoValue_WriteHoldingRegisterFloat.GsonTypeAdapter(gson);
    }
}
