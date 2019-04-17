package com.nubeiot.buildscript.docker.internal

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
@EqualsAndHashCode(includes = ["name", "tag"])
class OperatingSystem implements Serializable {

    static final OperatingSystem DEBIAN = new OperatingSystem("debian", "debian", "stretch", "Dockerfile.template")
    static final OperatingSystem SLIM = new OperatingSystem("debian", "slim", "stretch-slim", "Dockerfile.template")
    static final OperatingSystem ALPINE = new OperatingSystem("alpine", "alpine", "3.9", "Dockerfile.alpine.template")

    final String name
    final String tag
    final String version
    final String dockerfile

    def dockerImage() {
        return "$name:$version"
    }

    @CompileStatic
    @TupleConstructor
    static class OpenJdkOperatingSystem extends OperatingSystem {
        final String javaVersion
        final JavaType javaType

        OpenJdkOperatingSystem(OperatingSystem os, String javaVersion, JavaType javaType) {
            super(os.name, os.tag, os.version, os.dockerfile)
            this.javaVersion = javaVersion
            this.javaType = javaType
        }

        def dockerImage() {
            if (name != "alpine") {
                return "openjdk:$javaVersion-$javaType.value-$version"
            }
            return "openjdk:$javaVersion-$javaType.value-$name$version"
        }
    }

}
