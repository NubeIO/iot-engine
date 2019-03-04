package com.nubeiot.core.jni;

import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public class JniUnit extends UnitVerticle<JniConfig, JniContext> {

    JniUnit() {
        super(new JniContext());
    }

    @Override
    public void start() {
        super.start();
        logger.info("Loading JNI config...");
        logger.debug(config.toJson());
        this.loadLib(config);
    }

    @Override
    public @NonNull Class<JniConfig> configClass() {
        return JniConfig.class;
    }

    @Override
    public @NonNull String configFile() {
        return "jni.json";
    }

    private void loadLib(JniConfig config) {
        // Runtime load native lib
        // For example when `config.getLib() = Example` then it will search for:
        // `Example.dll` in Windows, libExample.so in Unix, and libExample.dylib in Mac machine
        String filename = config.getLibDir() + "/" + getDynamicLib(config.getLib());
        try {
            System.load(filename);
        } catch (UnsatisfiedLinkError e) {
            throw new InitializerError(Strings.format("It seems like, we do not have file `{0}`", filename));
        }
    }

    private String getDynamicLib(String library) {
        String osName = System.getProperty("os.name").toLowerCase();
        String prefix;
        String postfix;
        if (osName.startsWith("linux")) {
            prefix = "lib";
            postfix = "so";
        } else if (osName.startsWith("mac os x")) {
            prefix = "lib";
            postfix = "dylib";
        } else {
            prefix = "";
            postfix = "dll";
        }
        return prefix + library + "." + postfix;
    }

}
