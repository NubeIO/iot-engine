package com.nubeiot.core.utils.mock;

import com.nubeiot.core.exceptions.ServiceException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class MockReflection {

    private final String id;
    @Setter
    private String name;

    public int methodNoArgument() {
        return 1;
    }

    public void throwNubeException(String hey) {
        throw new ServiceException(hey);
    }

    public void throwUnknownException(String hey) {
        throw new RuntimeException(hey);
    }

}
