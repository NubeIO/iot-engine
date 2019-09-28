package com.nubeiot.edge.connector.bacnet;

import com.nubeiot.edge.connector.bacnet.dto.BACnetMSTP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.serotonin.bacnet4j.transport.DefaultTransport;

final class TransportMstp implements TransportProvider {

    static TransportIP byConfig(BACnetMSTP config) {
        //    private Transport buildTransportMSTP(String port, int macNum, int bufferSize, int retryCount) throws
        //    Exception {
        //        byte[] bytes = new byte[bufferSize];
        //        MstpNode node = new MasterNode(port, new ByteArrayInputStream(bytes), new ByteArrayOutputStream(),
        //                                       (byte) macNum, retryCount);
        //        MstpNetwork network = new MstpNetwork(node, );
        //        return new DefaultTransport(network);
        //    }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DefaultTransport get() { throw new UnsupportedOperationException("Not yet implemented"); }

    @Override
    public BACnetNetwork config() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
