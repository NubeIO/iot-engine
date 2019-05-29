package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = BasicCredential.class, name = "BASIC"),
    @JsonSubTypes.Type(value = TokenCredential.class, name = "TOKEN"),
})
public abstract class Credential {

    @Getter
    private final CredentialType type;

    @Getter
    private final String user;

    public abstract String computeUrl(String defaultUrl);

    protected abstract String getPrefixUrl(String urlPrefix);

    public abstract String computeHeader();

    protected String computeRemoteUrl(String defaultUrl) {
        if (defaultUrl.startsWith("http://")) {
            return computeUrl(defaultUrl, "http://");
        }

        if (defaultUrl.startsWith("https://")) {
            return computeUrl(defaultUrl, "https://");
        }

        return defaultUrl;
    }

    private String computeUrl(String defaultUrl, String urlPrefix) {
        return defaultUrl.replaceFirst(urlPrefix, this.getPrefixUrl(urlPrefix));
    }

    @Override
    public String toString() {
        return "User: " + user + "::Type: " + type;
    }

    public enum CredentialType {
        BASIC, TOKEN;
    }

}
