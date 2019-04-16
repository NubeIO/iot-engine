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
public abstract class WriteHoldingRegisterString implements StringOperation, WriteOperation<String> {
    public abstract String value();

    public static Builder builder() {
        return new AutoValue_WriteHoldingRegisterString.Builder()
                .setType(Type.WRITE_HOLDING_REGISTER_STRING);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setValue(String value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setByteCount(int value);

        public abstract WriteHoldingRegisterString build();
    }

    public static TypeAdapter<WriteHoldingRegisterString> typeAdapter(Gson gson) {
        return new AutoValue_WriteHoldingRegisterString.GsonTypeAdapter(gson);
    }
}
