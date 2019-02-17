package com.nubeiot.buildscript.docker

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.command.PushImageResultCallback
import com.nubeiot.buildscript.ProjectUtils
import com.nubeiot.buildscript.docker.internal.RegistryCredentialsAware
import com.nubeiot.buildscript.docker.internal.rule.DockerImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.GCRImageTagRule

class DockerPushTask extends DockerTask implements RegistryCredentialsAware {

    @InputFile
    final RegularFileProperty indexFile = newInputFile()

    final Property<String> cloud = project.objects.property(String)

    @Override
    DockerClient createDockerClient(Project project) {
        return create().createDockerClient(project)
    }

    @Override
    String description() {
        return "Docker publish images"
    }

    DockerPushTask() {
        setOnlyIf {
            project.rootProject == project
        }
        indexFile.set(project.rootProject.buildDir.toPath().resolve("docker.txt").toFile())
    }

    @TaskAction
    void push() {
        File f = indexFile.getAsFile().get()
        if (!f.isFile() || !f.exists()) {
            logger.info("Not found index file: ${f.name}")
        }
        f.eachLine { line ->
            println line
            def rule = rule(line)
            if (Objects.nonNull(rule)) {
                println rule.images()
                client.tagImageCmd(line, rule.repository(), rule.tag()).exec()
                client.pushImageCmd(rule.repository()).withTag(rule.tag()).exec(new PushImageResultCallback())
                      .awaitCompletion()
            }
        }
    }

    @Internal
    DockerImageTagRule rule(String image) {
        if (cloud.getOrNull() == "gcr") {
            return new GCRImageTagRule(DockerImageTagRule.parse(image),
                                       ProjectUtils.extraProp(project, "dockerRegistryUrl"),
                                       ProjectUtils.extraProp(project, "dockerRegistryProject"))
        }
        return null
    }
}
