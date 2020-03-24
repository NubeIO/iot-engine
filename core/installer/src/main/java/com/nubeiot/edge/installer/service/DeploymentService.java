package com.nubeiot.edge.installer.service;

import com.nubeiot.core.event.EventListener;

/**
 * The interface Deployment service.
 *
 * @since 1.0.0
 */
public interface DeploymentService extends EventListener {

    /**
     * Gets shared data.
     *
     * @param <D>     Type of {@code data}
     * @param dataKey the data key
     * @return the data
     * @since 1.0.0
     */
    <D> D sharedData(String dataKey);

}
