package com.nubeiot.buildscript.docker

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.nubeiot.buildscript.ProjectUtils

class DockerTask extends DefaultTask {
    @Input
    Map<String, String> baseImages = new HashMap<>()
    @Input
    String jvmOptions = "-Xms:1g -Xmx:1g"

    @Override
    Task dependsOn(Object... paths) {
        return super.dependsOn(paths) + project.tasks.build
    }

    @TaskAction
    void build() {
        baseImages.each { k, v -> println "${k}:${v}" }
        def destFile = "${project.distsDir}/Dockerfile"
        def artifactName = ProjectUtils.computeBaseName(project)
        new Dockerfile(destFile: destFile)
    }
}
