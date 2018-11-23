package com.nubeiot.dashboard.impl;

import com.nubeiot.core.common.utils.Runner;

import static com.nubeiot.dashboard.constants.Location.WEB_SERVER_MICRO_SERVICE_LOCATION;

// Test class' working directory points HOME_DIRECTORY, but actually it should be one step lower than that
public class HttpServerVerticleIDERun extends HttpServerVerticle {
    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-web-server/src/main/java/";
        Runner.runExample(JAVA_DIR, HttpServerVerticleIDERun.class);
    }

    @Override
    public String getRootFolder() {
        return System.getProperty("user.dir") + "/nube-web-server" + WEB_SERVER_MICRO_SERVICE_LOCATION;
    }
}
