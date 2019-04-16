package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class ReadOutputCoil implements ValueOperation {
    public static ReadOutputCoil.Builder builder() {
        return new AutoValue_ReadOutputCoil.Builder().setType(Type.READ_OUTPUT_COIL);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setType(Type value);

        public abstract Builder setConnection(Connection value);

        public abstract Builder setOffset(int value);

        public abstract ReadOutputCoil build();
    }

    public static TypeAdapter<ReadOutputCoil> typeAdapter(Gson gson) {
        return new AutoValue_ReadOutputCoil.GsonTypeAdapter(gson);
    }
}
