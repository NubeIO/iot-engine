package com.nubeiot.buildscript.docker.internal;

import org.gradle.api.Project;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.nubeiot.buildscript.ProjectUtils;

public interface RegistryCredentialsAware extends DockerHostAware {

    static String dockerRegistryUrl(Project project) {
        return ProjectUtils.extraProp(project, "dockerRegistryUrl", "index.docker.io/v2/");
    }

    default DockerClientConfig clientConfig(Project project) {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                                        .withDockerHost(DockerHostAware.dockerHost(project))
                                        .withRegistryUrl(dockerRegistryUrl(project))
                                        .withRegistryUsername(ProjectUtils.extraProp(project, "dockerRegistryUser"))
                                        .withRegistryPassword(ProjectUtils.extraProp(project, "dockerRegistryPwd"))
                                        .withRegistryEmail(ProjectUtils.extraProp(project, "dockerRegistryEmail"))
                                        .build();
    }

    static RegistryCredentialsAware create() {
        return new RegistryCredentialsAware() {};
    }

}
