package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ExternalServer extends HostInfo {

    private final String url;
    @Setter
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
        int port = hosts.length > 1 ? Strings.convertToInt(hosts[1], ssl ? 443 : 80) : ssl ? 443 : 80;
        return HostInfo.builder().host(hosts[0]).port(port).ssl(ssl).build();
    }

}
