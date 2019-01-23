package com.nubeiot.buildscript.docker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerTask extends DefaultTask {
    @Input
    List<String> baseImages

    @TaskAction
    void build() {

    }
}
