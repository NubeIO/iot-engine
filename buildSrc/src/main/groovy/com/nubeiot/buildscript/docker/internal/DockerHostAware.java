package com.nubeiot.buildscript.docker.internal;

import org.gradle.api.Project;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.nubeiot.buildscript.ProjectUtils;

public interface DockerHostAware extends DockerAware {

    default DockerClientConfig clientConfig(Project project) {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                                        .withDockerHost(ProjectUtils.extraProp(project, "dockerHost"))
                                        .build();
    }

    default DockerClient createDockerClient(Project project) {
        return DockerClientBuilder.getInstance(clientConfig(project)).build();
    }

    static DockerHostAware create() {
        return new DockerHostAware() {};
    }

}
