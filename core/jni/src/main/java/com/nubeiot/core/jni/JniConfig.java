package com.nubeiot.core.jni;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.utils.FileUtils;

import lombok.Getter;

@Getter
public class JniConfig implements IConfig {

    public static final String NAME = "__jni__";
    public static final String DEFAULT_LIB = "library";

    private String libDir = FileUtils.DEFAULT_DATADIR.toString();
    private String lib = DEFAULT_LIB;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
