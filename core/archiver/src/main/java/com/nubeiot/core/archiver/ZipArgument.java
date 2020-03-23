package com.nubeiot.core.archiver;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.lingala.zip4j.model.ZipParameters;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class ZipArgument implements JsonData {

    private final JsonObject trackingInfo;
    private final boolean appendTimestamp;
    private final String overriddenDestFileName;
    private final String password;
    private final long watcherDelayInMilli;
    @NonNull
    private final ZipParameters zipParameters;

    public static ZipArgument createDefault(JsonObject trackingInfo) {
        return ZipArgument.builder()
                          .trackingInfo(trackingInfo)
                          .appendTimestamp(true)
                          .watcherDelayInMilli(100)
                          .zipParameters(defaultZipParameters())
                          .build();
    }

    public static ZipArgument noTimestamp() {
        return noTimestamp(null);
    }

    public static ZipArgument noTimestamp(JsonObject trackingInfo) {
        return ZipArgument.builder()
                          .trackingInfo(trackingInfo)
                          .appendTimestamp(false)
                          .watcherDelayInMilli(100).zipParameters(defaultZipParameters()).build();
    }

    static @NonNull ZipParameters defaultZipParameters() {
        ZipParameters parameters = new ZipParameters();
        parameters.setIncludeRootFolder(false);
        return parameters;
    }

    public char[] toPassword() {
        return Strings.isBlank(password) ? null : password.toCharArray();
    }

}
