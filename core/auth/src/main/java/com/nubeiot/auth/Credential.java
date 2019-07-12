package com.nubeiot.auth;

import static com.nubeiot.core.IConfig.recomputeReferences;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.SecretConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = BasicCredential.class, name = "BASIC"),
    @JsonSubTypes.Type(value = BasicSecretCredential.class, name = "BASIC_SECRET"),
    @JsonSubTypes.Type(value = TokenCredential.class, name = "TOKEN"),
})
public abstract class Credential {

    @Getter
    private final CredentialType type;

    public abstract String computeUrl(String defaultUrl);

    abstract String computeUrlCredential();

    public abstract String computeHeader();

    String computeRemoteUrl(String defaultUrl) {
        return defaultUrl.replaceFirst("^((https?|wss?)://)(.+)", "$1" + this.computeUrlCredential() + "$3");
    }

    @SuppressWarnings("unchecked")
    public static <C extends IConfig> C recomputeReferenceCredentials(C config, SecretConfig secretConfig) {
        return IConfig.from(recomputeReferences(config.toJson(), (jObject, key, value) -> {
            if (key.equals("type") && value.equals("BASIC")) {
                jObject.put("type", "BASIC_SECRET");
            } else if (value.startsWith("@secret")) {
                jObject.put(key,
                            new JsonObject().put("ref", value).put("value", secretConfig.decode(value).getValue()));
            }
        }), (Class<C>) config.getClass());
    }

    @Override
    public String toString() {
        return "Type: " + type;
    }

    public enum CredentialType {
        BASIC, BASIC_SECRET, TOKEN
    }

}
