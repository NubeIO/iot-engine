package com.nubeiot.buildscript.docker

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal

import com.github.dockerjava.api.DockerClient
import com.nubeiot.buildscript.ProjectUtils
import com.nubeiot.buildscript.docker.internal.DockerAware

import groovy.transform.CompileStatic

@CompileStatic
abstract class DockerTask extends DefaultTask implements DockerAware {

    private static final String DOCKER_ENV_FILE = "docker.secret.properties"

    @Internal
    DockerClient client

    @Internal
    abstract String description()

    DockerTask() {
        setOnlyIf {
            ProjectUtils.extraProp(project, "dockerable", "false") == "true"
        }
        setGroup("docker")
        setDescription(description())
        ProjectUtils.loadSecretProps(project, "$project.rootDir/$DOCKER_ENV_FILE")
        client = createDockerClient(project)
    }

}
