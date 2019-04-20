package com.nubeiot.buildscript.docker.internal

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
enum JavaEdition {

    OPENJDK("openjdk"), ORACLE("oraclejdk")

    final String value

}
