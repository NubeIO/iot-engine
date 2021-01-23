package com.nubeiot.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public final class BasicCredential extends Credential {

    @Getter
    private final String password;

    @JsonCreator
    public BasicCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                           @JsonProperty(value = "user", required = true) String user,
                           @JsonProperty(value = "password", required = true) String password) {
        super(type, user);
        this.password = password;
    }

    @Override
    public String toUrl(String url) {
        return this.computeRemoteUrl(url);
    }

    @Override
    public String toHeader() {
        final byte[] combine = (this.getUser() + ":" + this.getPassword()).getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(combine);
    }

    @Override
    protected String computeUrlCredential() {
        return this.getUser() + ":" + this.getPassword() + "@";
    }

    @Override
    public String toString() {
        return super.toString() + "::Password:*****";
    }

}
