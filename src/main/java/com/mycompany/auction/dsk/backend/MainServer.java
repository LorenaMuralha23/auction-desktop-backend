package com.mycompany.auction.dsk.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer implements Runnable {

    private ServerSocket serverSocket;
    

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);

            while (true) {
                System.out.println("Esperando Conexão...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado!");

                // Cria fluxo de entrada para receber mensagens
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String mensagem;
                while ((mensagem = in.readLine()) != null) {
                    System.out.println("Mensagem recebida: " + mensagem);

                    // Converte a mensagem JSON em um objeto Java (supondo que seja um JSON com chave "nome" e "idade")
                    ObjectMapper objectMapper = new ObjectMapper();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    JsonNode jsonNode = objectMapper.readTree(mensagem);
                    
                    String response = mapOperation(jsonNode.get("operation").asText());
                    
                    out.println(response);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        startServer();
    }
    
    public String mapOperation(String operation){
        String response = "";
        switch (operation) {
            case "LOGIN":
                response = createResponseJson();
                break;
        }
        
        return response;
    }
    
    public String createResponseJson(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseNode = objectMapper.createObjectNode();

            // Adiciona dados à resposta JSON
            ((ObjectNode) responseNode).put("group_address", Main.multicastService.getMulticastGroup());
            ((ObjectNode) responseNode).put("group_port", Main.multicastService.getPort());
            ((ObjectNode) responseNode).put("login_status", "SUCCESS");

            // Retorna a string JSON
            return objectMapper.writeValueAsString(responseNode);

        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; 
        }
    }
    
}
