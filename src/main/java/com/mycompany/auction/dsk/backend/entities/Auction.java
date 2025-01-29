package com.mycompany.auction.dsk.backend.entities;

import java.time.LocalDateTime;

public class Auction {

    private Product currentProduct;
    private String currentWinner;
    private int currentPrice;
    private LocalDateTime timeToStart;
    private LocalDateTime timeToEnd;

    public Auction() {
    }

    public Auction(Product currentProduct, String currentWinner, int currentBid) {
        this.currentProduct = currentProduct;
        this.currentWinner = currentWinner;
        this.currentPrice = currentBid;
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }

    
    public String getCurrentWinner() {
        return currentWinner;
    }

    public void setCurrentWinner(String currentWinner) {
        this.currentWinner = currentWinner;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getTimeToStart() {
        return timeToStart;
    }

    public void setTimeToStart(LocalDateTime timeToStart) {
        this.timeToStart = timeToStart;
    }

    public LocalDateTime getTimeToEnd() {
        return timeToEnd;
    }

    public void setTimeToEnd(LocalDateTime timeToEnd) {
        this.timeToEnd = timeToEnd;
    }

    

    @Override
    public String toString() {
        return "Auction{" + "currentProduct=" + currentProduct + ", currentWinner=" + currentWinner + ", currentBid=" + currentPrice + '}';
    }
    
    
}
