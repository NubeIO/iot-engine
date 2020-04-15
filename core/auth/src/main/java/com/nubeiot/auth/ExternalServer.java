package com.nubeiot.auth;

import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nubeiot.core.http.base.HostInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;

@Getter
@JsonDeserialize
@ToString(onlyExplicitlyIncluded = true)
public class ExternalServer extends HostInfo {

    @Include
    private final String url;
    @Setter
    @Include
    private Credential credential;

    public ExternalServer(String url) {
        super(build(url));
        this.url = url;
    }

    @JsonCreator
    public ExternalServer(@JsonProperty(value = "url", required = true) String url,
                          @JsonProperty(value = "credential") Credential credential) {
        this(url);
        this.credential = credential;
    }

    private static HostInfo build(String url) {
        String[] hosts = Strings.requireNotBlank(url).replaceAll("https?://", "").split(":");
        boolean ssl = url.matches("^https");
        int port = ssl ? 443 : 80;
        if (hosts.length > 1) {
            port = Strings.convertToInt(hosts[1], port);
        }
        return HostInfo.builder().host(hosts[0]).port(port).ssl(ssl).build();
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put("url", url).put("credential", credential.toJson());
    }

}
