package com.nubeiot.core;

import java.util.HashMap;

import com.nubeiot.core.utils.Strings;

public abstract class SecretConfig extends HashMap<String, String> implements IConfig {

    public static final String NAME = "__secret__";

    @Override
    public String name() { return NAME; }

    public SecretProperty decode(String key) {
        String value = key;
        if (key.startsWith("@secret.")) {
            key = key.replaceAll("^@secret.", "@");
        }
        value = Strings.getFirstNotNull(this.get(key), value);
        return new SecretProperty(key, value);
    }

}
