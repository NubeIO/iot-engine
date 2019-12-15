package com.nubeiot.core.workflow;

import java.util.Objects;

public interface TaskExecutionData<D> extends TaskContext {

    D getData();

    Throwable getThrowable();

    default boolean isError() {
        return Objects.nonNull(getThrowable());
    }

}
