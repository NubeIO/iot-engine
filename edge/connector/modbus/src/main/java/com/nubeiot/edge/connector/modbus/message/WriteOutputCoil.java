package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class WriteOutputCoil implements WriteOperation<Boolean> {
    public abstract Boolean value();

    public static WriteOutputCoil.Builder builder() {
        return new AutoValue_WriteOutputCoil.Builder().setType(Type.WRITE_OUTPUT_COIL);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract Builder setValue(Boolean value);

        public abstract WriteOutputCoil build();
    }

    public static TypeAdapter<WriteOutputCoil> typeAdapter(Gson gson) {
        return new AutoValue_WriteOutputCoil.GsonTypeAdapter(gson);
    }
}
