package com.nubeiot.buildscript.docker.internal.rule;

import java.util.Set;
import java.util.stream.Collectors;

public interface DockerImageTagRuleDecorator extends DockerImageTagRule {

    DockerImageTagRule getRule();

    String prefixImage(String image);

    String suffixImage(String image);

    @Override
    default String repository() {
        return prefixImage(getRule().repository());
    }

    @Override
    default String tag() {
        return check(suffixImage(getRule().tag()));
    }

    @Override
    default Set<String> images() {
        return getRule().images().stream().map(it -> prefixImage(suffixImage(it))).collect(Collectors.toSet());
    }

}
