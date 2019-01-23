package com.nubeiot.buildscript

import org.gradle.api.Project

class ProjectUtils {
    def static computeGroup(Project project) {
        if (project.parent == null) {
            return project.group
        }
        def suffix = project.parent == project.rootProject ? "" : ("." + project.parent.name)
        return computeGroup(project.parent) + suffix
    }

    def static computeBaseName(Project project) {
        if (project.parent == null) {
            return project.ext.baseName
        }
        return computeBaseName(project.parent) + "-" + project.name
    }

    def static loadSecretProps(Project project, secretFile) {
        def sf = new File(secretFile.toString())
        if (sf.exists()) {
            def props = new Properties()
            sf.withInputStream { props.load(it) }
            props.each { k, v -> project.ext.set(k, v) }
        }
    }
}
