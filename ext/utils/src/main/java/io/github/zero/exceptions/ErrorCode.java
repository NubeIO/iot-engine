package io.github.zero.exceptions;

import java.io.Serializable;

import lombok.NonNull;

public interface ErrorCode extends Serializable {

    ErrorCode UNKNOWN_ERROR = new InternalErrorCode("UNKNOWN_ERROR");
    ErrorCode HIDDEN = new InternalErrorCode("HIDDEN");
    ErrorCode FILE_ERROR = new InternalErrorCode("FILE_ERROR");
    ErrorCode REFLECTION_ERROR = new InternalErrorCode("REFLECTION_ERROR");
    ErrorCode URL_ERROR = new InternalErrorCode("URL_ERROR");

    @NonNull String code();

}
