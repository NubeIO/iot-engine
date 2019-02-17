package com.nubeiot.buildscript.docker.internal.rule;

abstract class ImageTagRuleDecorator implements DockerImageTagRuleDecorator {

    final DockerImageTagRule rule;

    protected ImageTagRuleDecorator(DockerImageTagRule rule) {this.rule = rule;}

    @Override
    public DockerImageTagRule getRule() {
        return rule;
    }

}
