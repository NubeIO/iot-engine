package com.nubeiot.core.cluster;

import com.nubeiot.core.dto.IRequestData;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClusterNode implements JsonData, IRequestData {

    private final String id;
    private final String name;
    private final String address;
    private final String localAddress;

}
