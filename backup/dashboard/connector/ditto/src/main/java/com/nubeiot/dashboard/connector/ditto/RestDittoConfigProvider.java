package com.nubeiot.dashboard.connector.ditto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RestDittoConfigProvider {

    @Getter
    private final DittoConfig dittoConfig;

}
