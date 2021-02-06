package com.nubeiot.edge.connector.bacnet.dto;

import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@FieldNameConstants
public class CovOutput implements JsonData {

    private final String key;
    private final JsonObject cov;

    CovOutput(String key, JsonObject cov) {
        this.key = key;
        this.cov = cov;
    }

    @Override
    public JsonObject toJson(@NonNull ObjectMapper mapper) {
        return new JsonObject().put(key, cov);
    }

    public static class CovOutputBuilder {

        JsonObject any;

        public CovOutputBuilder any(JsonObject any) {
            this.any = any;
            return this;
        }

        public CovOutput build() {
            return new CovOutput(key, new JsonObject().put(Fields.cov, cov).mergeIn(any));
        }

    }

}
