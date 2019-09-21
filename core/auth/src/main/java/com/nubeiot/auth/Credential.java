package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.nubeiot.core.dto.JsonData;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = BasicCredential.class, name = "BASIC"),
    @JsonSubTypes.Type(value = TokenCredential.class, name = "TOKEN"),
})
public abstract class Credential implements JsonData {

    @Getter
    private final CredentialType type;

    @Getter
    private final String user;

    public abstract String computeUrl(String defaultUrl);

    protected abstract String computeUrlCredential();

    public abstract String computeHeader();

    protected String computeRemoteUrl(String defaultUrl) {
        return defaultUrl.replaceFirst("^((https?|wss?)\\:\\/\\/)(.+)", "$1" + this.computeUrlCredential() + "$3");
    }

    @Override
    public String toString() {
        return "User: " + user + "::Type: " + type;
    }

    public enum CredentialType {
        BASIC, TOKEN
    }

}
