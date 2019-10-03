package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.nubeiot.core.dto.JsonData;

import lombok.Getter;
import lombok.NonNull;
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

    public abstract String toUrl(String url);

    public abstract String toHeader();

    protected abstract String computeUrlCredential();

    String computeRemoteUrl(String defaultUrl) {
        return defaultUrl.replaceFirst("^((https?|wss?)\\:\\/\\/)(.+)", "$1" + this.computeUrlCredential() + "$3");
    }

    @Override
    public String toString() {
        return "User: " + user + "::Type: " + type;
    }

    public static class HiddenCredential extends Credential {

        public HiddenCredential(@NonNull Credential credential) {
            super(credential.getType(), credential.getUser());
        }

        @Override
        public String toUrl(String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toHeader() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected String computeUrlCredential() {
            throw new UnsupportedOperationException();
        }

    }

}
