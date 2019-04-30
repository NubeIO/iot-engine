package com.nubeiot.core.http.rest.provider;

import com.nubeiot.core.NubeConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestConfigProvider {

    @Getter
    private final NubeConfig config;

}
