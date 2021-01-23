package com.nubeiot.core.http.rest.provider;

import com.nubeiot.core.http.HttpConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestHttpConfigProvider {

    @Getter
    private final HttpConfig httpConfig;

}
