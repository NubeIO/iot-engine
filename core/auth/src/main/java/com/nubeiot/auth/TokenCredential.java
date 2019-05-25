package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class TokenCredential extends Credential {

    @Getter
    private final String token;

    @JsonCreator
    public TokenCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                           @JsonProperty(value = "token", required = true) String token) {
        super(type);
        this.token = token;
    }

    @Override
    public String toString() {
        return "Token: ******************************";
    }

    @Override
    public String computeCredentialUrl() {
        return new StringBuilder(this.token).append("@").toString();
    }

}
