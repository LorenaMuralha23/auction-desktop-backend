package com.mycompany.auction.dsk.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.auction.dsk.backend.Main;
import com.mycompany.auction.dsk.backend.entities.Auction;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionController {

    private Set<String> clientsInTheRoom = new HashSet<>();
    private boolean auctionStatus = false;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private String winner;
    private int currentPrice;

    private final AuctionService auctionService;
    private Auction currentAuction;

    public AuctionController() {
        this.auctionService = new AuctionService();
        this.currentAuction = new Auction();
    }

    public boolean addClientIntoTheRoom(String clientToJoin) {
        if (!clientsInTheRoom.contains(clientToJoin)) {
            boolean isAdded = clientsInTheRoom.add(clientToJoin);

            if (isAdded) {
                System.out.println("[THERE ARE " + this.clientsInTheRoom.size() + " PLAYERS IN THE ROOM]");

                return isAdded;
            }
        }
        return false;
    }

    public void verifyIfCanStart() throws IOException, InterruptedException {
        if (clientsInTheRoom.size() >= 3 && !auctionStatus) {
            auctionStatus = true;
            this.auctionService.defineTimeToStartAndEnd();
            startGameAt(this.auctionService.getTimeToStart());
            endGameAt(this.auctionService.getTimeToEnd());
        }
    }

    public void startGameAt(LocalDateTime timeToStart) throws IOException {
        long delay = Duration.between(LocalDateTime.now(), timeToStart).toMillis();

        if (delay > 0) {
            scheduler.schedule(() -> {
                try {
                    startGame();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, delay, TimeUnit.MILLISECONDS);

            System.out.println("THE ROUND WILL START AT: " + timeToStart);
        } else {
            System.out.println("O horário de início já passou!");
        }
    }

    public void endGameAt(LocalDateTime timeToEnd) {
        long delay = Duration.between(LocalDateTime.now(), timeToEnd).toMillis();

        if (delay > 0) {
            scheduler.schedule(() -> {
                try {
                    endGame();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, delay, TimeUnit.MILLISECONDS);

            System.out.println("THE ROUND WILL END AT: " + timeToEnd);
        } else {
            System.out.println("O horário de término já passou!");
        }
    }

    public void startGame() throws IOException, InterruptedException {
        auctionService.definingInfoToAuction();
    }

    public void endGame() throws IOException, InterruptedException {
        this.auctionStatus = false;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();

        jsonNode.put("username", "server");
        jsonNode.put("operation", "FINISH ROUND");
        jsonNode.put("winner", this.currentAuction.getCurrentWinner());

        Main.multicastService.sendMessageToGroup(jsonNode.toString());

        Thread.sleep(10000);
        verifyIfCanStart();
    }

    public void updateInfoGame(JsonNode jsonNode) throws IOException, InterruptedException {
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
        return this.currentAuction;
    }

    public void updateAuction(String currentWinner, int currentPrice) {
        this.currentAuction.setCurrentWinner(currentWinner);
        this.currentAuction.setCurrentPrice(currentPrice);
    }

}
