package com.nubeiot.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = BasicCredential.class, name = "BASIC"),
    @JsonSubTypes.Type(value = TokenCredential.class, name = "TOKEN"),
})
public abstract class Credential {

    @Getter
    private final CredentialType type;

    public abstract String computeCredentialUrl();

    public enum CredentialType {
        BASIC, TOKEN;
    }

}
