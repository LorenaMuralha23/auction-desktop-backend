package com.mycompany.auction.dsk.backend.multicast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.auction.dsk.backend.Main;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MulticastService implements Runnable {

    private MulticastSocket socket;
    private InetAddress group = null;
    private final int port = 4446;
    private final String multicastGroup = "230.0.0.0";

    public void startMulticastGroup() throws IOException {
        this.socket = new MulticastSocket(port);
        group = InetAddress.getByName(multicastGroup);
        socket.joinGroup(group);

        System.out.println("[MULTICAST SERVER STARTED]");
    }

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public int getPort() {
        return port;
    }

    public void sendMessageToGroup(String message) throws IOException, InterruptedException {
        String encryptedMessage = Main.encryptService.encryptSymmetric(message);
        DatagramPacket packet = new DatagramPacket(encryptedMessage.getBytes(), encryptedMessage.length(), group, port);

        socket.send(packet);
    }

    public void listenForMessages() throws InterruptedException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] buffer = new byte[2048]; 
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {

                socket.receive(packet);

                if (packet.getLength() > 0 && Main.auctionController.isAuctionStatus()) {
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String decryptedMessage = Main.encryptService.decryptSymmetric(message);
                    
                    JsonNode jsonNode = objectMapper.readTree(decryptedMessage);

                    if (!jsonNode.get("username").asText().equals("server")) {
                        if (jsonNode.get("operation").asText().equals("RAISE BID")) {
                            Main.auctionController.updateInfoGame(jsonNode);
                        }

                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[JOGO FINALIZADO]");
    }

    @Override
    public void run() {
        try {
            listenForMessages();
        } catch (InterruptedException ex) {
            Logger.getLogger(MulticastService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
