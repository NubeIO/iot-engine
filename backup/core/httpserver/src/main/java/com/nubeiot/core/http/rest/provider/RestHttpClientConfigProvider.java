package com.nubeiot.core.http.rest.provider;

import com.nubeiot.core.http.client.HttpClientConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestHttpClientConfigProvider {

    @Getter
    private final HttpClientConfig httpClientConfig;

}
