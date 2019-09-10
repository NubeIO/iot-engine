package com.nubeiot.buildscript.docker.internal.rule

import org.gradle.api.Project

import com.nubeiot.buildscript.ProjectUtils

import groovy.transform.CompileStatic

@CompileStatic
class ProjectImageTagRule implements DockerImageTagRule {

    final Project project
    final String artifact
    final String repository

    ProjectImageTagRule(Project project) {
        this.project = project
        this.artifact = ProjectUtils.computeBaseName(project)
        this.repository = ProjectUtils.computeDockerName(project)
    }

    @Override
    String repository() {
        return repository
    }

    @Override
    String artifact() {
        return artifact
    }

    @Override
    String tag() {
        return "${project.version}.b${ProjectUtils.extraProp(project, "buildNumber")}"
    }

}
