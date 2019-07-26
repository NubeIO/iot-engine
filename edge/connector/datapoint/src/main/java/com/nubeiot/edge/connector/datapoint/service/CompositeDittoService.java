package com.nubeiot.edge.connector.datapoint.service;

import java.util.List;

public interface CompositeDittoService extends DittoService {

    List<DittoService> children();

}
