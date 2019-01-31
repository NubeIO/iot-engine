package com.nubeiot.buildscript.docker

import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.ArchitectureInternal
import org.gradle.nativeplatform.platform.internal.NativePlatforms

import groovy.transform.Canonical

@Canonical
class JavaDockerImage {

    private static final Set<NativePlatform> PLATFORMS = new NativePlatforms().getDefaultPlatformName()
    String arch
    Set<String> os
//    Set<NativePlatform> platforms

    def static check(NativePlatform platform) {
        def arch = (ArchitectureInternal) platform.architecture
        return platform.operatingSystem.isLinux() && (arch.isAmd64() || arch.isArm())
    }

    def image(String javaVersion) {

    }


//    static final JavaDockerImage LINUX = new JavaDockerImage("debian", PLATFORMS.findAll { check(it) })
//
//    static final JavaDockerImage LINUX_SLIM = new JavaDockerImage("slim", PLATFORMS.findAll { check(it) })
//
//    static final JavaDockerImage ALPINE = new JavaDockerImage("alpine", PLATFORMS.findAll { check(it) })
}
