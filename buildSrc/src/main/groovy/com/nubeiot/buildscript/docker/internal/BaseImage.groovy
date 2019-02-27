package com.nubeiot.buildscript.docker.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@EqualsAndHashCode(includes = "baseImage")
class BaseImage {

    final Arch arch
    final OperatingSystem os
    final String baseImage

//    public static final Set<BaseImage> EDGE = [image(Arch.ARM32V6, OperatingSystem.ALPINE),
//                                               image(Arch.ARM32V7, OperatingSystem.DEBIAN),
//                                               image(Arch.ARM32V7, OperatingSystem.SLIM),
//                                               image(Arch.ARM64V8, OperatingSystem.DEBIAN),
//                                               image(Arch.ARM64V8, OperatingSystem.SLIM),
//                                               image(Arch.ARM64V8, OperatingSystem.ALPINE)] as LinkedHashSet

    public static final Set<BaseImage> EDGES = [image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet

//    public static final Set<BaseImage> SERVERS = [image(Arch.LINUX64, OperatingSystem.DEBIAN),
//                                                  image(Arch.LINUX64, OperatingSystem.SLIM),
//                                                  image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet
    public static final Set<BaseImage> SERVERS = [image(Arch.LINUX64, OperatingSystem.ALPINE)] as LinkedHashSet


    private static BaseImage image(Arch arch, OperatingSystem os) {
        def image = arch.canonicalName + "/" + os.name + ":" + os.version
        return new BaseImage(arch, os, image)
    }
}
