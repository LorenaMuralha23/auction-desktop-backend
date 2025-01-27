package com.mycompany.auction.dsk.backend.services;

import com.mycompany.auction.dsk.backend.Main;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionController {

    private Set<String> clientsInTheRoom = new HashSet<>();
    private boolean auctionStatus = false;

    private final AuctionService auctionService = new AuctionService();

    public boolean addClientIntoTheRoom(String clientToJoin) {
        if (!clientsInTheRoom.contains(clientToJoin)) {
            boolean isAdded = clientsInTheRoom.add(clientToJoin);

            if (isAdded) {
                System.out.println("There are " + this.clientsInTheRoom.size() + " players in the room...");

                return isAdded;
            }
        }
        return false;
    }

    public void verifyIfCanStart() throws IOException, InterruptedException {
        if (clientsInTheRoom.size() >= 3 && !auctionStatus) {
            System.out.println("Vou iniciar...");
            startGame();
        }
    }

    public void startGame() throws IOException, InterruptedException {
        if (!auctionStatus) {
            Thread.sleep(1000);
            this.auctionStatus = true;
            auctionService.definingInfoToAuction();
            Thread multicastGroup = new Thread(Main.multicastService);
            multicastGroup.start();
        }
    }

    public boolean isAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(boolean auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

}
