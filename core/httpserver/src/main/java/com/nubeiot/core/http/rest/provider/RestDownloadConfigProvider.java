package com.nubeiot.core.http.rest.provider;

import com.nubeiot.core.http.HttpConfig.FileStorageConfig.DownloadConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestDownloadConfigProvider {

    @Getter
    private final DownloadConfig downloadConfig;

}
