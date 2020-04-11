package com.nubeiot.core.component;

import java.nio.file.Path;

import io.github.zero.utils.Strings;

public interface UnitVerticleTestHelper {

    static void injectTest(UnitVerticle verticle, String sharedKey, Path path) {
        verticle.injectTest(Strings.isBlank(verticle.getSharedKey()) ? sharedKey : verticle.getSharedKey(), path);
    }

}
