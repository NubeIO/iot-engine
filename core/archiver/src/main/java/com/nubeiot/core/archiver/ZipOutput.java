package com.nubeiot.core.archiver;

import java.time.OffsetDateTime;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.DateTimes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ZipOutput.Builder.class)
public final class ZipOutput implements JsonData {

    private final JsonObject trackingInfo;
    private final String originFile;
    private final String zipFile;
    private final long size;
    private final OffsetDateTime lastModified;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        public Builder lastModified(long lastModified) {
            this.lastModified = DateTimes.from(lastModified);
            return this;
        }

        @JsonProperty("lastModified")
        public Builder lastModified(OffsetDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

    }

}
