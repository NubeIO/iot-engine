package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class BasicCredential extends Credential {

    @Getter
    private final String user;
    @Getter
    private final String password;

    @JsonCreator
    public BasicCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                           @JsonProperty(value = "user", required = true) String user,
                           @JsonProperty(value = "password", required = true) String password) {
        super(type);
        this.user = user;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User: " + user + "::Password:*****";
    }

    @Override
    public String computeCredentialUrl() {
        return new StringBuilder(this.user).append(":").append(this.password).append("@").toString();
    }

}
