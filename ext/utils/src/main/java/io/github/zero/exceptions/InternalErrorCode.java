package io.github.zero.exceptions;

import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
final class InternalErrorCode implements ErrorCode {

    private final String code;

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $code = this.code();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        return result;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ErrorCode)) {
            return false;
        }
        final ErrorCode other = (ErrorCode) o;
        final Object this$code = this.code();
        final Object other$code = other.code();
        return Objects.equals(this$code, other$code);
    }

}
