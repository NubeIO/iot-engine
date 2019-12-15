package com.nubeiot.core.exceptions;

import java.io.Serializable;

import com.nubeiot.core.dto.EnumType;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class EnumErrorCode implements EnumType, Serializable {

    public static EnumErrorCode DESIRED_ERROR = new EnumErrorCode("DESIRED_ERROR");
    public static EnumErrorCode INVALID_ARGUMENT = new EnumErrorCode("INVALID_ARGUMENT");
    public static EnumErrorCode ALREADY_EXIST = new EnumErrorCode("ALREADY_EXIST");
    public static EnumErrorCode NOT_FOUND = new EnumErrorCode("NOT_FOUND");
    public static EnumErrorCode SECURITY_ERROR = new EnumErrorCode("SECURITY_ERROR");
    public static EnumErrorCode AUTHENTICATION_ERROR = new EnumErrorCode("AUTHENTICATION_ERROR");
    public static EnumErrorCode INSUFFICIENT_PERMISSION_ERROR = new EnumErrorCode("INSUFFICIENT_PERMISSION_ERROR");
    public static EnumErrorCode HTTP_ERROR = new EnumErrorCode("HTTP_ERROR");
    public static EnumErrorCode SERVICE_ERROR = new EnumErrorCode("SERVICE_ERROR");
    public static EnumErrorCode INITIALIZER_ERROR = new EnumErrorCode("INITIALIZER_ERROR");
    public static EnumErrorCode ENGINE_ERROR = new EnumErrorCode("ENGINE_ERROR");
    public static EnumErrorCode CLUSTER_ERROR = new EnumErrorCode("CLUSTER_ERROR");
    public static EnumErrorCode EVENT_ERROR = new EnumErrorCode("EVENT_ERROR");
    public static EnumErrorCode DATABASE_ERROR = new EnumErrorCode("DATABASE_ERROR");
    public static EnumErrorCode STATE_ERROR = new EnumErrorCode("STATE_ERROR");
    public static EnumErrorCode UNKNOWN_ERROR = new EnumErrorCode("UNKNOWN_ERROR");
    public static EnumErrorCode TIMEOUT_ERROR = new EnumErrorCode("TIMEOUT_ERROR");
    public static EnumErrorCode NETWORK_ERROR = new EnumErrorCode("NETWORK_ERROR");
    public static EnumErrorCode COMMUNICATION_PROTOCOL_ERROR = new EnumErrorCode("COMMUNICATION_PROTOCOL_ERROR");
    private final String code;

    @Override
    public @NonNull String type() {
        return code;
    }

}
