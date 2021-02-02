package com.nubeiot.iotdata.unified;

import java.util.HashMap;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.protocol.Protocol;

import com.nubeiot.iotdata.IoTEntity;

import lombok.RequiredArgsConstructor;

//TODO: Need verify step
@RequiredArgsConstructor
public final class ParticularData<T extends IoTEntity> extends HashMap<Protocol, T> implements JsonData {

    private final Class<T> clazz;

}
