package com.mycompany.auction.dsk.backend.multicast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastService {

    private MulticastSocket socket;
    private InetAddress group = null;
    private final int port = 4446;
    private final String multicastGroup = "230.0.0.0";

    public void startMulticastGroup() throws IOException {
        this.socket = new MulticastSocket(port);
        group = InetAddress.getByName(multicastGroup);
        socket.joinGroup(group);

        System.out.println("Multicast server started!");
    }

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public int getPort() {
        return port;
    }
    
}