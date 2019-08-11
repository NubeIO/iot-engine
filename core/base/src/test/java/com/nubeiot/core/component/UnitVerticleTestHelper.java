package com.nubeiot.core.component;

import java.nio.file.Path;

public interface UnitVerticleTestHelper {

    static void injectTest(UnitVerticle verticle, String sharedKey, Path path) {
        verticle.injectTest(sharedKey, path);
    }

}
