package com.nubeiot.buildscript.docker.internal

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
enum JavaType {
    JDK("jdk"), JRE("jre")

    final String value

}
