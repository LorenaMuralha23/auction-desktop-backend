package com.mycompany.auction.dsk.backend.entities;


/**
 *
 * @author USER
 */

public class Product {
    private String name;
    private long startValue;
    private long minimumBid;
    private int year;

    public Product(String name, long startValue, long minimumBid, int year) {
        this.name = name;
        this.startValue = startValue;
        this.minimumBid = minimumBid;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartValue() {
        return startValue;
    }

    public void setStartValue(long startValue) {
        this.startValue = startValue;
    }

    public long getMinimumBid() {
        return minimumBid;
    }

    public void setMinimumBid(long minimumBid) {
        this.minimumBid = minimumBid;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Product{" + "name=" + name + ", startValue=" + startValue + ", minimumBid=" + minimumBid + ", year=" + year + '}';
    }
    
    
 
    
}
    
