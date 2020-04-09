package com.nubeiot.edge.installer.loader;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.NonNull;

public interface LocalExecutableArchiverModuleType extends ExecutableArchiverModuleType {

    LocalExecutableArchiverModuleType NODEJS_ARCHIVER = new LocalExecutableArchiverModuleType() {
        @Override
        public ExecutableBinaryModuleType binaryType() {
            return ExecutableBinaryModuleType.NODEJS_BINARY;
        }

        @Override
        public String type() {
            return "LOCAL_NODEJS_ARCHIVER";
        }
    };

    @JsonCreator
    static LocalExecutableArchiverModuleType factory(@NonNull String type) {
        return ModuleTypeFactory.factory(type, LocalExecutableArchiverModuleType.class);
    }

    @Override
    default @NonNull String protocol() {
        return "file";
    }

}
