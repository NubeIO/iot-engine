package com.nubeiot.buildscript.docker

import org.gradle.internal.os.OperatingSystem
import org.gradle.nativeplatform.platform.Architecture

import groovy.transform.Canonical

@Canonical
class JavaArchVersion {

    OperatingSystem os
    Architecture arch
    String version

}
