package com.nubeiot.buildscript.docker

import java.nio.file.Path
import java.util.stream.Collectors

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.nubeiot.buildscript.ProjectUtils

import groovy.json.JsonSlurper

class DockerBuildTask extends DefaultTask {

    static final def JAVA_VERSIONS = new JsonSlurper().
        parse(DockerBuildTask.class.getClassLoader().getResourceAsStream("oracle-jdk.json")) as Map

    @InputDirectory
    final DirectoryProperty buildDir = newInputDirectory()
    @Input
    final Property<String> javaVersion = project.objects.property(String)
    @Input
    @Optional
    final Property<String> jdkUrl = project.objects.property(String)
    @Input
    @Optional
    final Property<String> jvmOptions = project.objects.property(String)
    @Input
    @Optional
    final Property<String> javaProps = project.objects.property(String)
    @OutputFile
    final RegularFileProperty out = newOutputFile()

    DockerBuildTask() {
        ProjectUtils.loadSecretProps(project, "$project.rootDir/docker.secret.properties")
        setGroup("docker")
        setDescription("Docker Build Image")
        dependsOn(project.tasks.findByName("build"))
        buildDir.set(project.distsDir)
        out.set(project.rootProject.buildDir.toPath().resolve("docker.txt").toFile())
        javaVersion.set("8u201")
        jvmOptions.set("-Xms1g -Xmx1g")
        javaProps.set("")
    }

    @TaskAction
    void start() {
        println("Build docker image for ${project}")
        def baseImages = ProjectUtils.isSubProject(project, "edge") ? BaseImage.EDGE : BaseImage.SERVER
        def artifact = ProjectUtils.computeBaseName(project)
        def client = createDockerClient()
        baseImages.each { it ->
            def dockerfile = createDockerFile(it, artifact + "-" + project.version)
            def tags = computeTags(it, artifact)
            String imageId = client.buildImageCmd()
                                   .withBaseDirectory(buildDir.asFile.get())
                                   .withDockerfile(dockerfile.toFile())
                                   .withPull(true).withForcerm(true).withRemove(true)
                                   .withQuiet(false)
                                   .withTags(tags)
                                   .exec(new BuildImageResultCallback()).awaitImageId()
            println("- Build Docker image successfully with image id ${imageId} - Tags: ${tags}")
            out.asFile.get().append(tags.join("\n"))
            out.asFile.get().append("\n")
        }
    }

    @Internal
    Set<String> computeTags(BaseImage it, String artifactName) {
        def imageName = "${it.arch.canonicalName}/${artifactName}"
        def version = project.version + (it.os.tag ? ("-" + it.os.tag) : "") + "-" + project.property("buildNumber")
        def versions = ProjectUtils.extraProp(project, "docker.tags", "").split()
        return Arrays.stream(versions)
                     .map { t -> "${imageName}:${t}${it.os.tag ? '-' + it.os.tag : ''}".toString() }
                     .collect(Collectors.toSet()) + ["${imageName}:${version}".toString()]
    }

    @Internal
    DockerClient createDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                                                             .withDockerHost((String) project.property("dockerHost"))
                                                             .build()
        return DockerClientBuilder.getInstance(config).build()
    }

    @Internal
    Path createDockerFile(BaseImage it, String artifactName) {
        def dockerfile = it.os.dockerfile
        def imageBase = it.baseImage
        def defaultUrl = (String) JAVA_VERSIONS.get(javaVersion.get(), [:]).
            find { v -> it.arch.isAlias((String) v.key) }?.value
        def jdkUrl = jdkUrl.getOrElse(defaultUrl)
        if (!jdkUrl) {
            throw new TaskExecutionException(this, new RuntimeException("Not found jdk url"))
        }
        def arch = it.arch.canonicalName
        project.copy {
            into buildDir.get().asFile
            from("${project.rootDir}/docker/${dockerfile}") {
                rename '(.+)\\.template', "\$1.${arch}"
                filter {
                    it.replaceAll("\\{\\{IMAGE_BASE\\}\\}", imageBase)
                      .replaceAll("\\{\\{JDK_URL\\}\\}", jdkUrl)
                      .replaceAll("\\{\\{ARTIFACT\\}\\}", artifactName)
                      .replaceAll("\\{\\{JAVA_VERSION\\}\\}", javaVersion.get())
                      .replaceAll("\\{\\{JVM_OPTS\\}\\}", jvmOptions.get())
                      .replaceAll("\\{\\{JAVA_PROPS\\}\\}", javaProps.get())
                }
            }
        }
        return buildDir.get().asFile.toPath().resolve(dockerfile.replaceAll("(.+)\\.template", "\$1.${arch}"))
    }
}
