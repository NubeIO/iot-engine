package com.nubeiot.buildscript.docker.internal

import com.nubeiot.buildscript.docker.internal.OperatingSystem.OpenJdkOperatingSystem

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@EqualsAndHashCode(includes = "dockerImage")
class DockerBaseImage {

    final Arch arch
    final OperatingSystem os
    final String dockerImage

//    public static final Set<BaseImage> EDGE = [image(Arch.ARM32V6, OperatingSystem.ALPINE),
//                                               image(Arch.ARM32V7, OperatingSystem.DEBIAN),
//                                               image(Arch.ARM32V7, OperatingSystem.SLIM),
//                                               image(Arch.ARM64V8, OperatingSystem.DEBIAN),
//                                               image(Arch.ARM64V8, OperatingSystem.SLIM),
//                                               image(Arch.ARM64V8, OperatingSystem.ALPINE)] as LinkedHashSet

    public static final Set<DockerBaseImage> EDGES = [image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet

//    public static final Set<BaseImage> SERVERS = [image(Arch.LINUX64, OperatingSystem.DEBIAN),
//                                                  image(Arch.LINUX64, OperatingSystem.SLIM),
//                                                  image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet
    public static final Set<DockerBaseImage> SERVERS = [image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet


    static DockerBaseImage image(Arch arch, OperatingSystem os) {
        return new DockerBaseImage(arch, os, compute(arch, os))
    }

    static openjdkImage(DockerBaseImage image, String javaVersion, JavaType javaType) {
        OpenJdkOperatingSystem os = new OpenJdkOperatingSystem(image.os, javaVersion, javaType)
        return new DockerBaseImage(image.arch, os, compute(image.arch, os))
    }

    private static String compute(Arch arch, OperatingSystem os) {
        return "$arch.canonicalName/" + os.dockerImage()
    }
}
