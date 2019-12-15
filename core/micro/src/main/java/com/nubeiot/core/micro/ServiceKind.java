package com.nubeiot.core.micro;

public enum ServiceKind {

    LOCAL, CLUSTER;

    static ServiceKind parse(String scope) {
        if (CLUSTER.name().equalsIgnoreCase(scope)) {
            return CLUSTER;
        }
        return LOCAL;
    }
}
