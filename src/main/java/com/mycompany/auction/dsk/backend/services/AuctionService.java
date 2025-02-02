package com.mycompany.auction.dsk.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.auction.dsk.backend.Main;
import com.mycompany.auction.dsk.backend.entities.Product;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AuctionService {

    private static Product[] products = new Product[5];
    private Set<Integer> productsAuctioned = new HashSet<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode jsonNode = objectMapper.createObjectNode();

    private LocalDateTime timeToStart;
    private LocalDateTime timeToEnd;
    
    public AuctionService() {
        initiateProducts();
    }

    public void initiateProducts() {
        Product p1 = new Product("Range Rover Sport", 86600, 8660, 2024);
        Product p2 = new Product("BMW Série 7", 99900, 9990, 2022);
        Product p3 = new Product("Porsche Panamera", 88000, 8800, 2021);
        Product p4 = new Product("Lexus LS", 76000, 7600, 2019);
        Product p5 = new Product("Mercedes-Benz S-Class", 115000, 15000, 2023);

        products[0] = p1;
        products[1] = p2;
        products[2] = p3;
        products[3] = p4;
        products[4] = p5;
    }

    public Product definingProductToAuction() {
        Random random = new Random();
        int productToAuctioned = random.nextInt(5);
        while (!productsAuctioned.add(productToAuctioned)) {
            productToAuctioned = random.nextInt(5);
        }

        return products[productToAuctioned];
    }

    public void definingInfoToAuction() throws IOException, InterruptedException {
        // Inicializa o ObjectMapper e o ObjectNode
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();

        // Define o produto para leilão
        Product productToAuction = definingProductToAuction();

        // Define os tempos de início e fim
        // Adiciona informações ao JSON
        jsonNode.put("username", "server");
        jsonNode.put("operation", "SET INFO");
        jsonNode.put("product", productToAuction.getName());
        jsonNode.put("start_price", productToAuction.getStartValue());
        jsonNode.put("minimumBid", productToAuction.getMinimumBid());
        jsonNode.put("current-price", productToAuction.getStartValue());
        jsonNode.put("current-winner", "/");
        jsonNode.put("timeToEnd", this.timeToEnd.toString());
        
        Main.auctionController.getCurrentAuction().setCurrentProduct(productToAuction);
        

        System.out.println(jsonNode.toString()); // Corrigido para exibir a string JSON completa
        Main.multicastService.sendMessageToGroup(jsonNode.toString());
        
    }
    
    public void defineTimeToStart(){
        LocalDateTime now = LocalDateTime.now();
        this.timeToStart = now.plusMinutes(1);
        this.timeToEnd = this.timeToStart.plusMinutes(2);
        Main.auctionController.getCurrentAuction().setTimeToStart(timeToStart);
        Main.auctionController.getCurrentAuction().setTimeToEnd(timeToEnd);
    }

    public LocalDateTime getTimeToStart() {
        return timeToStart;
    }

    public LocalDateTime getTimeToEnd() {
        return timeToEnd;
    }

    
    
}
