package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.vertx.core.Vertx;

import lombok.NonNull;

//TODO implement it
public final class DeviceSubscriber /*extends AbstractProtocolSubscriber<EdgeDeviceComposite>
    implements BACnetSubscriber<EdgeDeviceComposite>*/ {

    DeviceSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        /*super(vertx, sharedKey);*/
    }

    //    @Override
    //    public @NonNull Class<EdgeDeviceComposite> context() {
    //        return EdgeDeviceMetadata.INSTANCE;
    //    }
    //
    //    @Override
    //    protected Single<EdgeDeviceComposite> doCreate(@NonNull EdgeDeviceComposite pojo) {
    //        throw new UnsupportedOperationException("Not yet supported create BACnet device");
    //    }
    //
    //    @Override
    //    protected Single<EdgeDeviceComposite> doUpdate(@NonNull EdgeDeviceComposite pojo) {
    //        throw new UnsupportedOperationException("Not yet supported update BACnet device");
    //    }
    //
    //    @Override
    //    protected Single<EdgeDeviceComposite> doPatch(@NonNull EdgeDeviceComposite pojo) {
    //        throw new UnsupportedOperationException("Not yet supported patch BACnet device");
    //    }
    //
    //    @Override
    //    protected Single<EdgeDeviceComposite> doDelete(@NonNull EdgeDeviceComposite pojo) {
    //        throw new UnsupportedOperationException("Not yet supported delete BACnet device");
    //    }
}
