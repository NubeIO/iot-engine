package com.nubeiot.core.micro;

public enum ServiceDiscoveryKind {

    LOCAL, CLUSTER;

    static ServiceDiscoveryKind parse(String scope) {
        if (CLUSTER.name().equalsIgnoreCase(scope)) {
            return CLUSTER;
        }
        return LOCAL;
    }
}
