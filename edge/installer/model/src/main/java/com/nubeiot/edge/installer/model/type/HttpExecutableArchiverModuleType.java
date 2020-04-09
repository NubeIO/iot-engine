package com.nubeiot.edge.installer.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.NonNull;

public interface HttpExecutableArchiverModuleType extends LocalExecutableArchiverModuleType {

    HttpExecutableArchiverModuleType HTTP_NODEJS_ARCHIVER = new HttpExecutableArchiverModuleType() {
        @Override
        public ExecutableBinaryModuleType binaryType() {
            return LocalExecutableArchiverModuleType.NODEJS_ARCHIVER.binaryType();
        }

        @Override
        public String type() {
            return "HTTP_NODEJS_ARCHIVER";
        }
    };

    @JsonCreator
    static HttpExecutableArchiverModuleType factory(@NonNull String type) {
        return ModuleTypeFactory.factory(type, HttpExecutableArchiverModuleType.class);
    }

    @Override
    default @NonNull String protocol() {
        return "http";
    }

}
