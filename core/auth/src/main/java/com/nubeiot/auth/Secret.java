package com.nubeiot.auth;

import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Secret {

    private static Secret secret = null;
    @Getter
    private final SecretConfig secretConfig;

    public static Secret getInstance() {
        if (secret == null) {
            secret = new Secret(new SecretConfig());
        }
        return secret;
    }

    public static void setInstance(SecretConfig secretConfig) {
        secret = new Secret(secretConfig);
    }

    public static String decode(String key) {
        String value = key;
        if (key.startsWith("@secret.")) {
            key = key.replaceAll("^@secret.", "@");
            value = "";
        }
        return Strings.getFirstNotNull(Secret.getInstance().getSecretConfig().toJson().getString(key), value);
    }

}
