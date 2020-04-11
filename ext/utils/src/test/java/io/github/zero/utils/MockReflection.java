package io.github.zero.utils;

import io.github.zero.exceptions.FileException;

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

    public void throwSneakyException(String hey) {
        throw new FileException(hey);
    }

    public void throwUnknownException(String hey) {
        throw new RuntimeException(hey);
    }

}
