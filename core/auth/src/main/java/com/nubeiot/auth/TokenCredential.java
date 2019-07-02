package com.nubeiot.auth;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.utils.Strings;

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
    public String decryptedUser() {
        return Strings.getFirstNotNull(Secret.getInstance().getSecretConfig().toJson().getString(this.getUser()),
                                       this.getUser());
    }

    @Override
    public String computeUrlCredential() {
        return (Objects.nonNull(this.getUser()) ? this.decryptedUser() + ":" : "") + this.decryptedToken() + "@";
    }

    @Override
    public String computeUrl(String defaultUrl) {
        return this.computeRemoteUrl(defaultUrl);
    }

    private String decryptedToken() {
        return Strings.getFirstNotNull(Secret.getInstance().getSecretConfig().toJson().getString(this.getToken()),
                                       this.getToken());
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
