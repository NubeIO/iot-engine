package com.nubeiot.buildscript.docker.internal.rule

import java.util.stream.Collectors

import org.gradle.api.Project

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor(callSuper = true, includeSuperProperties = true, includeSuperFields = true)
class CustomImageTagRule extends ProjectImageTagRule {

    final List<String> tags

    CustomImageTagRule(Project project, String tags) {
        super(project)
        this.tags = tags.split() as List
    }

    @Override
    Set<String> images() {
        return tags.stream().map { it -> repository() + ":" + check(it) }.collect(Collectors.toSet())
    }

}
