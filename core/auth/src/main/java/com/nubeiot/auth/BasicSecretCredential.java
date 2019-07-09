package com.nubeiot.auth;

import java.nio.charset.Charset;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.SecretProperty;

import lombok.Getter;

public class BasicSecretCredential extends Credential {

    @Getter
    private final SecretProperty user;

    @Getter
    private final SecretProperty password;

    @JsonCreator
    public BasicSecretCredential(@JsonProperty(value = "type", required = true) CredentialType type,
                                 @JsonProperty(value = "user", required = true) SecretProperty user,
                                 @JsonProperty(value = "password", required = true) SecretProperty password) {
        super(type);
        this.user = user;
        this.password = password;
    }

    @Override
    public String computeUrl(String defaultUrl) {
        return this.computeRemoteUrl(defaultUrl);
    }

    @Override
    public String computeUrlCredential() {
        return this.getUser().getValue() + ":" + this.getPassword().getValue() + "@";
    }

    @Override
    public String computeHeader() {
        return "Basic " + Base64.getEncoder()
                                .encodeToString(
                                    (this.getUser().getValue() + ":" + this.getPassword().getValue()).getBytes(
                                        Charset.forName("UTF-8")));
    }

    @Override
    public String toString() {
        return super.toString() + " :: User: " + user.getValue() + " :: Password:*****";
    }

}
