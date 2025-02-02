package com.mycompany.auction.dsk.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer implements Runnable {

    private ServerSocket serverSocket;

    public void startServer() throws InterruptedException {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(() -> processClient(clientSocket)).start();
            }

        } catch (IOException ex) {
            System.out.println("Deu algum erro...");
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processClient(Socket clientSocket) {
        try (
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String mensagem;
            while ((mensagem = in.readLine()) != null) {

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(mensagem);

                String response = mapOperation(jsonNode.get("operation").asText(), jsonNode);

                out.println(response);
                
                Main.auctionController.verifyIfCanStart();
                
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String mapOperation(String operation, JsonNode message) throws IOException, InterruptedException {
        String response = "";
        switch (operation) {
            case "LOGIN":
                if (Main.auctionController.addClientIntoTheRoom(message.get("username").asText())) {
                    response = createResponseJson();
                }
                break;
        }

        return response;
    }

    public String createResponseJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseNode = objectMapper.createObjectNode();

            // Adiciona dados à resposta JSON
            ((ObjectNode) responseNode).put("group_address", Main.multicastService.getMulticastGroup());
            ((ObjectNode) responseNode).put("group_port", Main.multicastService.getPort());
            ((ObjectNode) responseNode).put("login_status", "SUCCESS");
            ((ObjectNode) responseNode).put("auction_status", Main.auctionController.isAuctionStatus());
            //adicionar a chave pública do servidor

//            if (Main.auctionController.isAuctionStatus()) {//estiver iniciado se o jogo já estiver iniciado;
//                
//            }
            // Retorna a string JSON
            return objectMapper.writeValueAsString(responseNode);

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
    
}
