package com.nubeiot.core.transport;

public interface ProxyService<T extends Transporter> {

    T transporter();

}
