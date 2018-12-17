package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.nubeiot.core.event.EventType;

import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public final class EventBusRestMetadata {

    private static final List<HttpMethod> SINGULAR_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE,
                                                                                HttpMethod.PUT, HttpMethod.PATCH);
    private static final Function<EventBusRestMetadata, String> DEFAULT_GEN_PATH = metadata -> {
        HttpMethod method = metadata.getMethod();
        EventType action = metadata.getAction();
        if (HttpMethod.POST == method || (HttpMethod.GET == method && EventType.GET_LIST == action)) {
            return metadata.rawPath() + "s";
        }
        if (SINGULAR_HTTP_METHODS.contains(method)) {
            return metadata.rawPath() + "/:" + metadata.getParamName();
        }
        return metadata.rawPath();
    };

    private final String address;
    private final EventType action;
    private final String path;
    private final HttpMethod method;
    private final String paramName;
    private final Object forward;
    private final Function<EventBusRestMetadata, String> generatePath;

    public String rawPath() {
        return this.path;
    }

    public String getPath() {
        return Objects.isNull(this.generatePath) ? DEFAULT_GEN_PATH.apply(this) : this.generatePath.apply(this);
    }

}
