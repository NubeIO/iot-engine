package com.nubeiot.dashboard;

public enum Role {
    SUPER_ADMIN(1),
    ADMIN(2),
    MANAGER(3),
    USER(4),
    GUEST(5)
    ;

    private final int code;

    private Role(int code) {
        this.code = code;
    }
}
