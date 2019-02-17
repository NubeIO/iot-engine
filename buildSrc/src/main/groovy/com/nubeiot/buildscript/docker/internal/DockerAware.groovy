package com.nubeiot.buildscript.docker.internal

import org.gradle.api.Project

import com.github.dockerjava.api.DockerClient

interface DockerAware {

    DockerClient createDockerClient(Project project)

}
