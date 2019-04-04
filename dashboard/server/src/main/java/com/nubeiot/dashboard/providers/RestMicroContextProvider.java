package com.nubeiot.dashboard.providers;

import com.nubeiot.core.micro.MicroContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RestMicroContextProvider {

    private final MicroContext microContext;

}
