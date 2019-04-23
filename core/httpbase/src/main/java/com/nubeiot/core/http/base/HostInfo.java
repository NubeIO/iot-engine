package com.nubeiot.core.http.base;

import io.vertx.core.http.RequestOptions;
import io.vertx.core.shareddata.Shareable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = HostInfo.Builder.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HostInfo implements JsonData, Shareable {

    @Include
    private final String host;
    @Include
    private final int port;
    @Include
    private final boolean ssl;

    protected HostInfo(String host, int port, boolean ssl) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
    }

    public static HostInfo from(RequestOptions options) {
        return HostInfo.builder().host(options.getHost()).port(options.getPort()).ssl(options.isSsl()).build();
    }

    public RequestOptions to(String path) {
        return new RequestOptions().setHost(host).setPort(port).setSsl(ssl).setURI(path);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        public HostInfo build() {
            int port = this.port == 0 ? this.ssl ? 443 : 80 : this.port;
            return new HostInfo(this.host, port, this.ssl);
        }

    }

}
