package com.nubeiot.auth;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class TokenCredential extends Credential {

    @Getter
    private final String token;

    @JsonCreator
    public TokenCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                           @JsonProperty(value = "user", required = false) String user,
                           @JsonProperty(value = "token", required = true) String token) {
        super(type, user);
        this.token = token;
    }

    @Override
    public String computeUrl(String defaultUrl) {
        return this.computeRemoteUrl(defaultUrl);
    }

    @Override
    public String getUrlCredential() {
        return new StringBuilder(Objects.nonNull(this.getUser()) ? this.getUser() + ":" : "").append(this.getToken())
                                                                                             .append("@")
                                                                                             .toString();
    }

    @Override
    public String computeHeader() {
        return "Bearer " + this.getToken();
    }

    @Override
    public String toString() {
        return super.toString() + "::Token: ******************************";
    }

}
