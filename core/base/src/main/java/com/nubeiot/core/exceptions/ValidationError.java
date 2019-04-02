package com.nubeiot.core.exceptions;

import com.nubeiot.core.utils.Strings;

import lombok.Builder;

@Builder(builderClassName = "Builder")
public class ValidationError {

    static final NubeException.ErrorCode DEFAULT_ERROR_CODE = NubeException.ErrorCode.INVALID_ARGUMENT;
    static final String DEFAULT_ERROR_TYPE = "ValidationError";

    @lombok.Builder.Default
    private NubeException.ErrorCode errorCode = DEFAULT_ERROR_CODE;
    @lombok.Builder.Default
    private String errorType = DEFAULT_ERROR_TYPE;
    @lombok.Builder.Default
    private String value = "";
    private String message;

    public NubeException execute() {
        String errorMessage = (((Strings.isNotBlank(errorType) ? errorType + ": " : "") + value).trim() + " " + message)
                                  .trim();
        return new NubeException(errorCode, errorMessage);
    }

}
