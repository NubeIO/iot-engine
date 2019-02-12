package com.nubeiot.buildscript.docker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.nubeiot.buildscript.ProjectUtils

class DockerPushTask extends DefaultTask {

    DockerPushTask() {
        setGroup("docker")
        setDescription("Docker Push Image")
    }

    @TaskAction
    void push() {
        ProjectUtils.loadSecretProps(project, "$project.rootDir/docker.secret.properties")
    }

}
