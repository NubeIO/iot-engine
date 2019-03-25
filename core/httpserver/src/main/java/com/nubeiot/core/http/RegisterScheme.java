package com.nubeiot.core.http;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.HttpScheme;

import lombok.Getter;

public class RegisterScheme {

    private static RegisterScheme instance;
    @Getter
    private HttpScheme httpScheme;

    public static RegisterScheme getInstance() {
        if (instance == null) {
            throw new NubeException(NubeException.ErrorCode.INITIALIZER_ERROR, "You haven't registered the Scheme!");
        }
        return instance;
    }

    public void register(HttpScheme scheme) {
        this.httpScheme = scheme;
        if (instance == null) {
            instance = this;
        }
    }

}
