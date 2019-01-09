package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.utils.Urls;

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
public final class RestEventMetadata {

    private static final List<HttpMethod> SINGULAR_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE,
                                                                                HttpMethod.PUT, HttpMethod.PATCH);
    private static final Function<RestEventMetadata, String> DEFAULT_GEN_PATH = metadata -> {
        HttpMethod method = metadata.method;
        EventAction action = metadata.action;
        if (metadata.pathPattern) {
            return Urls.capturePatternPath(metadata.rawPath(), metadata.paramNames.toArray(new String[] {}));
        }
        if (HttpMethod.POST == method || (HttpMethod.GET == method && EventAction.GET_LIST == action)) {
            return metadata.rawPath() + "s";
        }
        if (SINGULAR_HTTP_METHODS.contains(method)) {
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

    @Default
    private final boolean local = false;

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
    private final Function<RestEventMetadata, String> generatePath = DEFAULT_GEN_PATH;

    public String rawPath() {
        return this.path;
    }

    public String getPath() {
        return Objects.isNull(this.generatePath) ? DEFAULT_GEN_PATH.apply(this) : this.generatePath.apply(this);
    }

}
