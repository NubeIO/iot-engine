package com.nubeiot.buildscript.docker.internal.rule

import com.nubeiot.buildscript.docker.internal.Arch
import com.nubeiot.buildscript.docker.internal.JavaEdition
import com.nubeiot.buildscript.docker.internal.OperatingSystem

class JavaTagRule extends ArchTagRule {

    final JavaEdition edition

    JavaTagRule(DockerImageTagRule rule, Arch arch, OperatingSystem os, OperatingSystem defaultOS, JavaEdition edition) {
        super(rule, arch, os, defaultOS)
        this.edition = edition
    }

    String prefixImage(String repository) {
        return "${arch.canonicalName}/${edition.value}/${repository}"
    }

}
