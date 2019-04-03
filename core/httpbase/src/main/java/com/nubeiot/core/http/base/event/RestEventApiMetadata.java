package com.nubeiot.core.http.base.event;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.HttpUtils.HttpMethods;
import com.nubeiot.core.http.base.Urls;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 *
 */
@Getter
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class RestEventApiMetadata {

    private static final Function<RestEventApiMetadata, String> DEFAULT_GEN_PATH = metadata -> {
        HttpMethod method = metadata.method;
        EventAction action = metadata.action;
        if (metadata.pathPattern) {
            return Urls.capturePatternPath(metadata.rawPath(), metadata.paramNames.toArray(new String[] {}));
        }
        if (HttpMethods.isSingular(method) && EventAction.GET_LIST != action) {
            return Urls.capturePath(metadata.rawPath(), metadata.paramNames.toArray(new String[] {}));
        }
        return metadata.rawPath();
    };

    @NonNull
    @EqualsAndHashCode.Include
    private final String address;

    @NonNull
    @EqualsAndHashCode.Include
    private final EventAction action;

    @NonNull
    private final EventPattern pattern;

    @NonNull
    private final HttpMethod method;

    @NonNull
    private final String path;

    @Default
    private final boolean pathPattern = false;

    @NonNull
    @Singular("paramName")
    private final List<String> paramNames;

    @Default
    private final Function<RestEventApiMetadata, String> generatePath = DEFAULT_GEN_PATH;

    public String rawPath() {
        return this.path;
    }

    public String getPath() {
        return Objects.isNull(this.generatePath) ? DEFAULT_GEN_PATH.apply(this) : this.generatePath.apply(this);
    }

}
