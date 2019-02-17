package com.nubeiot.buildscript.docker.internal;

import org.gradle.api.Project;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.nubeiot.buildscript.ProjectUtils;

public interface RegistryCredentialsAware extends DockerHostAware {

    default DockerClientConfig clientConfig(Project project) {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                                        .withDockerHost(ProjectUtils.extraProp(project, "dockerHost"))
                                        .withRegistryUrl(ProjectUtils.extraProp(project, "dockerRegistryUrl"))
                                        .withRegistryUsername(ProjectUtils.extraProp(project, "dockerRegistryUser"))
                                        .withRegistryPassword(ProjectUtils.extraProp(project, "dockerRegistryPwd"))
                                        .withRegistryEmail(ProjectUtils.extraProp(project, "dockerRegistryEmail"))
                                        .build();
    }

    static RegistryCredentialsAware create() {
        return new RegistryCredentialsAware() {};
    }

}
