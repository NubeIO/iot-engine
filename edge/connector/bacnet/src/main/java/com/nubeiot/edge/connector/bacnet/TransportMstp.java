package com.nubeiot.edge.connector.bacnet;

import com.nubeiot.edge.connector.bacnet.BACnetConfig.MSTPConfig;
import com.serotonin.bacnet4j.transport.DefaultTransport;

class TransportMstp implements TransportProvider {

    //    private Transport buildTransportMSTP(String port, int macNum, int bufferSize, int retryCount) throws
    //    Exception {
    //        byte[] bytes = new byte[bufferSize];
    //        MstpNode node = new MasterNode(port, new ByteArrayInputStream(bytes), new ByteArrayOutputStream(),
    //                                       (byte) macNum, retryCount);
    //        MstpNetwork network = new MstpNetwork(node, );
    //        return new DefaultTransport(network);
    //    }

    static TransportIP byConfig(MSTPConfig config) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DefaultTransport get() { throw new UnsupportedOperationException("Not yet implemented"); }

}
