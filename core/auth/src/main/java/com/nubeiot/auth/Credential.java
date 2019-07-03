package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = BasicCredential.class, name = "BASIC"),
    @JsonSubTypes.Type(value = TokenCredential.class, name = "TOKEN"),
})
public abstract class Credential {

    @Getter
    private final CredentialType type;

    @Getter
    private final String user;

    public abstract String computeUrl(String defaultUrl, SecretConfig secretConfig);

    abstract String computeUrlCredential(SecretConfig secretConfig);

    public abstract String computeHeader();

    String computeRemoteUrl(String defaultUrl, SecretConfig secretConfig) {
        return defaultUrl.replaceFirst("^((https?|wss?)://)(.+)",
                                       "$1" + this.computeUrlCredential(secretConfig) + "$3");
    }

    String decode(SecretConfig secretConfig, String key) {
        String value = key;
        if (key.startsWith("@secret.")) {
            key = key.replaceAll("^@secret.", "@");
            value = "";
        }
        return Strings.getFirstNotNull(secretConfig.toJson().getString(key), value);
    }

    @Override
    public String toString() {
        return "User: " + user + "::Type: " + type;
    }

    public enum CredentialType {
        BASIC, TOKEN
    }

}
