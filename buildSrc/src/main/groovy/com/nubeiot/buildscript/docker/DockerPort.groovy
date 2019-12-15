package com.nubeiot.buildscript.docker

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class DockerPort {
    int port
    Protocol type = Protocol.TCP

    @Override
    String toString() {
        return port + "/" + type
    }

    static DockerPort parse(String port) {
        String[] parts = port.split("/")
        return new DockerPort(validatePort(parts[0]), parts.length > 1 ? Protocol.parse(parts[1]) : Protocol.TCP)
    }

    static int validatePort(String port) {
        int p = Integer.parseInt(port)
        return p > 0 && p < 65536 ? p : 0
    }

    enum Protocol {
        TCP, UDP

        static Protocol parse(String protocol) {
            return Stream.of(values()).filter { it.name().equalsIgnoreCase(protocol) }.findFirst().orElse(TCP)
        }
    }
}
