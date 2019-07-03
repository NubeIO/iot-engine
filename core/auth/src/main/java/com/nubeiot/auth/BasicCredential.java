package com.nubeiot.auth;

import java.nio.charset.Charset;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.NubeConfig.SecretConfig;

import lombok.Getter;

public class BasicCredential extends Credential {

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
    public String computeUrl(String defaultUrl, SecretConfig secretConfig) {
        return this.computeRemoteUrl(defaultUrl, secretConfig);
    }

    @Override
    public String computeUrlCredential(SecretConfig secretConfig) {
        return decode(secretConfig, this.getUser()) + ":" + decode(secretConfig, this.getPassword()) + "@";
    }

    @Override
    public String computeHeader() {
        return "Basic " + Base64.getEncoder()
                                .encodeToString(
                                    (this.getUser() + ":" + this.getPassword()).getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public String toString() {
        return super.toString() + "::Password:*****";
    }

}
