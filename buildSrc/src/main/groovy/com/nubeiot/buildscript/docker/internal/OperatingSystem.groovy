package com.nubeiot.buildscript.docker.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@EqualsAndHashCode(includes = ["name", "tag"])
class OperatingSystem implements Serializable {

    static final OperatingSystem DEBIAN = new OperatingSystem("debian", "debian", "stretch", "Dockerfile.template")
    static final OperatingSystem SLIM = new OperatingSystem("debian", "slim", "stretch-slim",
                                                            "Dockerfile.slim.template")
    static final OperatingSystem ALPINE = new OperatingSystem("alpine", "alpine", "3.9", "Dockerfile.alpine.template")

    final name
    final String tag
    final String version
    final String dockerfile

}
