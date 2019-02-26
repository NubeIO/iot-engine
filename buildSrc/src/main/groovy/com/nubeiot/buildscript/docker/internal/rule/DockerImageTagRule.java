package com.nubeiot.buildscript.docker.internal.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.nubeiot.buildscript.Strings;

public interface DockerImageTagRule {

    String TAG_REGEX = "[a-zA-Z0-9_][a-zA-Z0-9_\\-\\.]{0,127}";

    String repository();

    String tag();

    default boolean autoCorrect() {
        return true;
    }

    default String correct(String tag) {
        String t = Strings.requireNotBlank(tag).replaceAll("[^a-zA-Z0-9_\\-\\.]+", "-").replaceAll("-+", "-");
        return t.length() > 128 ? t.substring(0, 128) : t;
    }

    default boolean validate(String tag) {
        return Pattern.matches(TAG_REGEX, tag);
    }

    default String check(String tag) {
        String t = Strings.requireNotBlank(tag);
        if (validate(t)) {
            return t;
        }
        if (autoCorrect()) {
            return correct(t);
        }
        throw new RuntimeException("Tag " + t + " is invalid");
    }

    default Set<String> images() {
        String tag = check(tag());
        if (Strings.isBlank(tag)) {
            return new HashSet<>();
        }
        return Collections.singleton(Strings.requireNotBlank(repository()) + ":" + tag);
    }

    static DockerImageTagRule parse(String image) {
        return new DockerImageTagRule() {
            @Override
            public String repository() {
                return image.substring(0, image.lastIndexOf(":"));
            }

            @Override
            public String tag() {
                return image.substring(image.lastIndexOf(":") + 1);
            }
        };
    }

}
