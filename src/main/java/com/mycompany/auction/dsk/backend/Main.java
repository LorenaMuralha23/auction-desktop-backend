package com.mycompany.auction.dsk.backend;

import com.mycompany.auction.dsk.backend.cryptography.EncryptService;
import com.mycompany.auction.dsk.backend.multicast.MulticastService;
import com.mycompany.auction.dsk.backend.services.AuctionController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static MainServer mainServer = new MainServer();
    public static MulticastService multicastService = new MulticastService();
    public static AuctionController auctionController = new AuctionController();
    public static EncryptService encryptService = new EncryptService();

    public static void main(String[] args) {
        try {
            multicastService.startMulticastGroup();
            Thread multicastGroup = new Thread(Main.multicastService);
            multicastGroup.start();

            Thread t1 = new Thread(mainServer);
            t1.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
