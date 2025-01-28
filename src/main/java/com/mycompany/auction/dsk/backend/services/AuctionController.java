package com.mycompany.auction.dsk.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.auction.dsk.backend.Main;
import com.mycompany.auction.dsk.backend.entities.Auction;
import com.mycompany.auction.dsk.backend.entities.Product;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionController {

    private Set<String> clientsInTheRoom = new HashSet<>();
    private boolean auctionStatus = false;
    
    private String winner;
    private int currentPrice;

    private final AuctionService auctionService = new AuctionService();
    private Auction currentAuction = new Auction();

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
        if (clientsInTheRoom.size() >= 2 && !auctionStatus) {
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
    
    public void updateInfoGame(JsonNode jsonNode) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonToSend = objectMapper.createObjectNode();
        
        currentPrice += jsonNode.get("bid").asInt();
        winner = jsonNode.get("username").asText();
        
        jsonToSend.put("username", "server");
        jsonToSend.put("operation", "UPDATE WINNER");
        jsonToSend.put("current-price", currentPrice);
        jsonToSend.put("current-winner", winner);
        
        updateAuction(winner, currentPrice);
        
        Main.multicastService.sendMessageToGroup(jsonToSend.toString());
    }

    public boolean isAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(boolean auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public Auction getCurrentAuction() {
        return currentAuction;
    }
    
    public void updateAuction(String currentWinner, int currentPrice){
        this.currentAuction.setCurrentWinner(currentWinner);
        this.currentAuction.setCurrentPrice(currentPrice);
    }
    
}
