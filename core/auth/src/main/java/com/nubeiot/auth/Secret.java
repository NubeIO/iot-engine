package com.nubeiot.auth;

import com.nubeiot.core.NubeConfig.SecretConfig;

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

}
