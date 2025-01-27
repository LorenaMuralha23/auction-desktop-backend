package com.mycompany.auction.dsk.backend;

import com.mycompany.auction.dsk.backend.multicast.MulticastService;
import com.mycompany.auction.dsk.backend.services.AuctionController;
import java.io.IOException;

public class Main {

    public static MainServer mainServer = new MainServer();
    public static MulticastService multicastService = new MulticastService();
    public static AuctionController auctionController = new AuctionController();

    public static void main(String[] args) throws IOException {
        multicastService.startMulticastGroup();
        
        Thread t1 = new Thread(mainServer);
        t1.start();
        
        
    }

}
