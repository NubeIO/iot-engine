package com.nubeiot.auth;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public final class TokenCredential extends Credential {

    @Getter
    private final String token;

    @JsonCreator
    public TokenCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                           @JsonProperty(value = "user") String user,
                           @JsonProperty(value = "token", required = true) String token) {
        super(type, user);
        this.token = token;
    }

    @Override
    public String toUrl(String url) {
        return this.computeRemoteUrl(url);
    }

    @Override
    public String toHeader() {
        return "Bearer " + this.getToken();
    }

    @Override
    protected String computeUrlCredential() {
        return (Objects.nonNull(this.getUser()) ? this.getUser() + ":" : "") + this.getToken() + "@";
    }

    @Override
    public String toString() {
        return super.toString() + "::Token: ******************************";
    }

}
