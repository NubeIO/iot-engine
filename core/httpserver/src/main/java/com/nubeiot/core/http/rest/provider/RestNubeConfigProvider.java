package com.nubeiot.core.http.rest.provider;

import com.nubeiot.core.NubeConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestNubeConfigProvider {

    @Getter
    private final NubeConfig nubeConfig;

}
