package com.nubeiot.buildscript.docker.internal.rule

import org.gradle.api.Project

import com.nubeiot.buildscript.ProjectUtils

import groovy.transform.CompileStatic

@CompileStatic
class ProjectImageTagRule implements DockerImageTagRule {

    final Project project
    final String repository

    ProjectImageTagRule(Project project) {
        this.project = project
        this.repository = ProjectUtils.computeBaseName(project)
    }

    @Override
    String repository() {
        return repository
    }

    @Override
    String tag() {
        return "${project.version}.b${ProjectUtils.extraProp(project, "buildNumber")}"
    }

}
