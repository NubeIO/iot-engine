package com.nubeiot.core.http.base;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;

import io.vertx.core.http.RequestOptions;
import io.vertx.core.shareddata.Shareable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = HostInfo.Builder.class)
public class HostInfo implements JsonData, Shareable {

    private final String host;
    private final int port;
    private final boolean ssl;

    protected HostInfo(String host, int port, boolean ssl) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

    public static HostInfo from(RequestOptions options) {
        return HostInfo.builder().host(options.getHost()).port(options.getPort()).ssl(options.isSsl()).build();
    }

    public RequestOptions to() {
        return new RequestOptions().setHost(host).setPort(port).setSsl(ssl);
    }

}
