package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class ExternalServer {

    private final String url;
    @Setter
    private Credential credential;

    @JsonCreator
    public ExternalServer(@JsonProperty(value = "credential") Credential credential,
                          @JsonProperty(value = "url", required = true) String url) {
        this.credential = credential;
        this.url = Strings.requireNotBlank(url);
    }

    public String computeUrl() {
        return this.credential.computeUrl(this.url);
    }

    public HostInfo toHost() {
        final String fullHost = url.replaceAll("https?://", "");
        final String[] hosts = fullHost.split(":");
        final int port = hosts.length > 1 ? Strings.convertToInt(hosts[1], 0) : 0;
        return HostInfo.builder().host(hosts[0]).port(port).ssl(url.matches("^https")).build();
    }

}
