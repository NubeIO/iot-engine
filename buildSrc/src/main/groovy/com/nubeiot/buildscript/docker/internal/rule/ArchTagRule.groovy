package com.nubeiot.buildscript.docker.internal.rule

import javax.annotation.Nonnull

import com.nubeiot.buildscript.docker.internal.Arch
import com.nubeiot.buildscript.docker.internal.OperatingSystem

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor(callSuper = true, includeSuperProperties = true, includeSuperFields = true)
class ArchTagRule extends ImageTagRuleDecorator {

    @Nonnull
    final Arch arch
    @Nonnull
    final OperatingSystem os
    @Nonnull
    final OperatingSystem defaultOS

    ArchTagRule(DockerImageTagRule rule, Arch arch, OperatingSystem os, OperatingSystem defaultOS) {
        super(rule)
        this.arch = arch
        this.os = os
        this.defaultOS = defaultOS
    }

    String suffixImage(String tag) {
        return tag + (defaultOS == os ? "" : "-" + os.tag)
    }

    String prefixImage(String repository) {
        return "${arch.canonicalName}/${repository}"
    }

}
