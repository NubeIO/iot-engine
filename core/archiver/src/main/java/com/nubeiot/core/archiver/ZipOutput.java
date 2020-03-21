package com.nubeiot.core.archiver;

import java.time.OffsetDateTime;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.DateTimes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public final class ZipOutput implements JsonData {

    private final String originFile;
    private final String zipFile;
    private final long size;
    private final OffsetDateTime lastModified;

    ZipOutput(String originFile, String zipFile, long size, OffsetDateTime lastModified) {
        this.originFile = originFile;
        this.zipFile = zipFile;
        this.size = size;
        this.lastModified = lastModified;
    }

    public static class Builder {

        public Builder lastModified(OffsetDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder lastModified(long lastModified) {
            this.lastModified = DateTimes.from(lastModified);
            return this;
        }

    }

}
