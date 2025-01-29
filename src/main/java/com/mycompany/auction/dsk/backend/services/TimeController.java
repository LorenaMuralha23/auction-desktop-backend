package com.mycompany.auction.dsk.backend.services;

import java.time.LocalDateTime;

public class TimeController implements Runnable{
    
    
    public void timer(LocalDateTime timeToEnd){
        while(LocalDateTime.now().isBefore(timeToEnd)) {
            //deve-se continuar aqui
        }
        System.out.println("O jogo finalizou!");
    }
    
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
