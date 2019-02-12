package com.nubeiot.buildscript.docker

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class OperatingSystem {

    static final OperatingSystem DEBIAN = new OperatingSystem("debian", "", "stretch", "Dockerfile.template")
    static final OperatingSystem SLIM = new OperatingSystem("debian", "slim", "stretch-slim",
                                                            "Dockerfile.slim.template")
    static final OperatingSystem ALPINE = new OperatingSystem("alpine", "alpine", "3.9", "Dockerfile.alpine.template")

    final String name
    final String tag
    final String version
    final String dockerfile

}
