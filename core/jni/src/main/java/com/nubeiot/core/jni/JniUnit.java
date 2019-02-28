package com.nubeiot.core.jni;

import java.lang.reflect.Field;

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

        try {
            this.setLibraryPath(config.getLibDir());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InitializerError(e.getMessage());
        }
        // Runtime load native lib
        // For example when `config.getLib() = Example` then it will search for:
        // `Example.dll` in Windows, libExample.so in Unix, and libExample.dylib in Mac machine
        try {
            System.loadLibrary(config.getLib());
        } catch (UnsatisfiedLinkError e) {
            throw new InitializerError(
                Strings.format("Library `{0}` does not seems to be in folder `{1}`", config.getLib(),
                               config.getLibDir()));
        }
    }

    private void setLibraryPath(String path) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("java.library.path", path);
        // Set sys_paths to null so that java.library.path will be reevaluated next time it is needed
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }

}
