package com.nubeiot.buildscript.docker

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class Arch {

    final String canonicalName
    final List<String> aliases

    boolean isAlias(String input) {
        return canonicalName == input || aliases.contains(input)
    }

    public static final Arch ARM32V6 = new Arch("arm32v6", ["armv6", "arm32"])
    public static final Arch ARM32V7 = new Arch("arm32v7", ["armv7", "arm32"])
    public static final Arch ARM64V8 = new Arch("arm64v8", ["armv8", "arm64"])
    public static final Arch LINUX64 = new Arch("amd64", ["linux64"])
    public static final Arch WIN64 = new Arch("windows-amd64", ["win64"])

}
