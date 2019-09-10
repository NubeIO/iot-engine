package com.nubeiot.buildscript.docker

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.gradle.api.Project
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
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.nubeiot.buildscript.ProjectUtils
import com.nubeiot.buildscript.Strings
import com.nubeiot.buildscript.docker.internal.DockerBaseImage
import com.nubeiot.buildscript.docker.internal.DockerHostAware
import com.nubeiot.buildscript.docker.internal.JavaEdition
import com.nubeiot.buildscript.docker.internal.JavaType
import com.nubeiot.buildscript.docker.internal.OperatingSystem
import com.nubeiot.buildscript.docker.internal.rule.CustomImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.DockerImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.GitFlowImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.JavaTagRule
import com.nubeiot.buildscript.docker.internal.rule.ProjectImageTagRule

import groovy.json.JsonSlurper

class DockerBuildTask extends DockerTask implements DockerHostAware {

    static final def JAVA_VERSIONS

    static {
        InputStream stream = DockerBuildTask.class.classLoader.getResourceAsStream("oraclejdk.json")
        JAVA_VERSIONS = new JsonSlurper().parse(stream) as Map
    }

    @InputDirectory
    final DirectoryProperty buildDir = newInputDirectory()
    @Input
    final Property<OperatingSystem> defaultOS = project.objects.property(OperatingSystem)
    @Input
    final Property<Boolean> pull = project.objects.property(Boolean)
    @Input
    final Property<String> javaVersion = project.objects.property(String)
    @Input
    final Property<JavaEdition> javaEdition = project.objects.property(JavaEdition)
    @Input
    final Property<JavaType> javaType = project.objects.property(JavaType)
    @Input
    @Optional
    final Property<String> jdkUrl = project.objects.property(String)
    @Input
    @Optional
    final Property<String> jvmOptions = project.objects.property(String)
    @Input
    @Optional
    final Property<String> javaProps = project.objects.property(String)
    @Input
    final Property<String> vcsBranch = project.objects.property(String)
    @OutputFile
    final RegularFileProperty out = newOutputFile()

    @Override
    DockerClient createDockerClient(Project project) {
        return create().createDockerClient(project)
    }

    @Override
    String description() {
        return "Docker Build Image"
    }

    DockerBuildTask() {
        dependsOn(project.tasks.findByName("build"))
        pull.set(false)
        defaultOS.set(OperatingSystem.ALPINE)
        buildDir.set(project.distsDir)
        out.set(project.rootProject.buildDir.toPath().resolve("docker.txt").toFile())
        javaVersion.set("8u201")
        javaEdition.set(JavaEdition.ORACLE)
        javaType.set(JavaType.JDK)
        jvmOptions.set("")
        javaProps.set("")
        vcsBranch.set("")
    }

    @TaskAction
    void start() {
        println("Build docker image for ${project}")
        def baseImages = ProjectUtils.isSubProject(project, "edge") ? DockerBaseImage.EDGES :
                         DockerBaseImage.SERVERS
        ProjectImageTagRule tagRule = new ProjectImageTagRule(project)
        List<DockerImageTagRule> rules = rules(tagRule)
        baseImages.each { it ->
            def dockerfile = createDockerFile(it, tagRule.artifact() + "-" + project.version)
            def tags = computeTags(it, rules)
            String imageId = client.buildImageCmd()
                                   .withBaseDirectory(buildDir.asFile.get())
                                   .withDockerfile(dockerfile.toFile())
                                   .withPull(pull.get()).withForcerm(true).withRemove(true)
                                   .withTags(tags)
                                   .exec(new BuildImageResultCallback()).awaitImageId()
            println("- Build Docker image successfully with image id ${imageId} - Tags: ${tags}")
            out.asFile.get().append(tags.join("\n"))
            out.asFile.get().append("\n")
        }
    }

    @Internal
    List<DockerImageTagRule> rules(ProjectImageTagRule rule) {
        List<DockerImageTagRule> rules = []
        String customTags = ProjectUtils.extraProp(project, "docker.tags")
        if (!Strings.isBlank(customTags)) {
            rules.add(new CustomImageTagRule(project, customTags))
        }
        if (!Strings.isBlank(vcsBranch.getOrElse(""))) {
            rules.add(new GitFlowImageTagRule(project, vcsBranch.get()))
        }
        if (rules.isEmpty()) {
            rules.add(rule)
        }
        return rules
    }

    @Internal
    Set<String> computeTags(DockerBaseImage image, List<DockerImageTagRule> rules) {
        return rules.stream()
                    .map { rule ->
                        new JavaTagRule(rule, image.arch, image.os, defaultOS.get(), javaEdition.get()).images()
                    }
                    .flatMap { x -> x.stream() }.collect(Collectors.toSet())
    }

    @Internal
    Path createDockerFile(DockerBaseImage baseImage, String artifactName) {
        def link = ""
        DockerBaseImage image = baseImage
        if (javaEdition.get() == JavaEdition.OPENJDK) {
            image = DockerBaseImage.openjdkImage(baseImage, javaVersion.get(), javaType.get())
        } else if (javaEdition.get() == JavaEdition.ORACLE) {
            def defaultUrl = (String) JAVA_VERSIONS.get(javaVersion.get(), [:])
                                                   .find { v -> image.arch.isAlias((String) v.key) }?.value
            link = jdkUrl.getOrElse(defaultUrl)
            if (!link) {
                throw new TaskExecutionException(this, new RuntimeException("Not found Oracle jdk url"))
            }
        } else {
            throw new TaskExecutionException(this, new RuntimeException("Unsupport platform ${javaEdition.get()}"))
        }
        return genDockerfile(image, artifactName, link)
    }

    @Internal
    Path genDockerfile(DockerBaseImage image, String artifactName, String jdkUrl = "") {
        def exposePorts = ProjectUtils.extraProp(project, "docker.exposePorts", "8000 5000 5701")
                                      .tokenize()
                                      .stream()
                                      .map { p -> Integer.valueOf(p) }
                                      .filter { p -> p > 0 && p < 65536 }
                                      .map { p -> p.toString() }
                                      .collect(Collectors.joining(" "))
        project.copy {
            into buildDir.get().asFile
            from("${project.rootDir}/docker/${Paths.get(javaEdition.get().value, image.os.dockerfile).toString()}") {
                rename '(.+)\\.template', "\$1.${image.arch.canonicalName}"
                filter {
                    it.replaceAll("\\{\\{IMAGE_BASE\\}\\}", image.dockerImage)
                      .replaceAll("\\{\\{JDK_URL\\}\\}", jdkUrl)
                      .replaceAll("\\{\\{ARTIFACT\\}\\}", artifactName)
                      .replaceAll("\\{\\{VERSION\\}\\}", project.version)
                      .replaceAll("\\{\\{JAVA_VERSION\\}\\}", javaVersion.get())
                      .replaceAll("\\{\\{JVM_OPTS\\}\\}", jvmOptions.get())
                      .replaceAll("\\{\\{JAVA_PROPS\\}\\}", javaProps.get())
                      .replaceAll("\\{\\{PORTS\\}\\}", exposePorts)
                }
            }
            from("${project.rootDir}/docker/entrypoint.sh")
            from("${project.rootDir}/docker/wait-for-it.sh")
        }
        return buildDir.get().asFile.toPath()
                       .resolve(image.os.dockerfile.replaceAll("(.+)\\.template", "\$1.${image.arch.canonicalName}"))
    }
}
