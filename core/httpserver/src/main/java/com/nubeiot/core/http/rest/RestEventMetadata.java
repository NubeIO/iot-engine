package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;

import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class RestEventMetadata {

    private static final List<HttpMethod> SINGULAR_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE,
                                                                                HttpMethod.PUT, HttpMethod.PATCH);
    private static final Function<RestEventMetadata, String> DEFAULT_GEN_PATH = metadata -> {
        HttpMethod method = metadata.getMethod();
        EventAction action = metadata.getAction();
        if (HttpMethod.POST == method || (HttpMethod.GET == method && EventAction.GET_LIST == action)) {
            return metadata.rawPath() + "s";
        }
        if (SINGULAR_HTTP_METHODS.contains(method)) {
            return metadata.rawPath() + "/:" + metadata.getParamName();
        }
        return metadata.rawPath();
    };

    @EqualsAndHashCode.Include
    private final String address;
    @EqualsAndHashCode.Include
    private final EventAction action;
    private final EventPattern pattern;
    private final boolean local;
    private final String path;
    private final HttpMethod method;
    private final String paramName;
    private final Function<RestEventMetadata, String> generatePath;

    public String rawPath() {
        return this.path;
    }

    public String getPath() {
        return Objects.isNull(this.generatePath) ? DEFAULT_GEN_PATH.apply(this) : this.generatePath.apply(this);
    }

}
