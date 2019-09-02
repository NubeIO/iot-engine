package com.nubeiot.dashboard.providers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestMediaDirProvider {

    @Getter
    private final String mediaAbsoluteDir;
    @Getter
    private final String mediaDir;

}
