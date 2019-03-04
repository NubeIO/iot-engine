package com.nubeiot.buildscript.docker.internal.rule

import com.nubeiot.buildscript.Strings

import groovy.transform.CompileStatic

@CompileStatic
class GCRImageTagRule extends ImageTagRuleDecorator {

    private static final List<String> HOSTS = Arrays.asList("gcr.io", "us.gcr.io", "eu.gcr.io", "asia.gcr.io")
    final String hostName
    final String projectId

    GCRImageTagRule(DockerImageTagRule rule, String hostName, String projectId) {
        super(rule)
        this.hostName = validateHost(hostName)
        this.projectId = Strings.requireNotBlank(projectId, "Project Id must be not blank")
    }

    @Override
    String prefixImage(String image) {
        return hostName + "/" + projectId + "/" + image
    }

    @Override
    String suffixImage(String image) {
        return image
    }

    @Override
    String repository() {
        return hostName + "/" + projectId + "/" + getRule().repository()
    }

    private static String validateHost(String hostName) {
        if (HOSTS.contains(hostName)) {
            return hostName
        }
        throw new IllegalArgumentException("Invalid Google cloud registry host")
    }

}
