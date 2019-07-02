package com.nubeiot.auth;

import java.nio.charset.Charset;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.utils.Strings;

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
    public String decryptedUser() {
        return Strings.getFirstNotNull(Secret.getInstance().getSecretConfig().toJson().getString(this.getUser()),
                                       this.getUser());
    }

    @Override
    public String computeUrlCredential() {
        return this.decryptedUser() + ":" + this.decryptedPassword() + "@";
    }

    @Override
    public String computeUrl(String defaultUrl) {
        return this.computeRemoteUrl(defaultUrl);
    }

    private String decryptedPassword() {
        return Strings.getFirstNotNull(Secret.getInstance().getSecretConfig().toJson().getString(this.getPassword()),
                                       this.getPassword());
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
