package com.mycompany.auction.dsk.backend.multicast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.auction.dsk.backend.Main;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastService implements Runnable {

    private MulticastSocket socket;
    private InetAddress group = null;
    private final int port = 4446;
    private final String multicastGroup = "230.0.0.0";

    public void startMulticastGroup() throws IOException {
        this.socket = new MulticastSocket(port);
        group = InetAddress.getByName(multicastGroup);
        socket.joinGroup(group);

        System.out.println("Multicast server started!");
    }

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public int getPort() {
        return port;
    }

    public void sendMessageToGroup(String message) throws IOException {
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, port);

        socket.send(packet);
        System.out.println("Mensagem JSON enviada para o grupo multicast!");
    }

    public void listenForMessages() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] buffer = new byte[256]; // Tamanho do buffer para armazenar pacotes recebidos
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Listening for messages in the multicast group...");
            socket.setSoTimeout(60000);

            while (Main.auctionController.isAuctionStatus()) {
                try {
                    socket.receive(packet);

                    if (packet.getLength() > 0) {
                        String message = new String(packet.getData(), 0, packet.getLength());
                        JsonNode jsonNode = objectMapper.readTree(message);

                        if (!jsonNode.get("username").asText().equals("server")) {
                            //impede que o proprio usuário veja a propria mensagem
                            System.out.println("Mensagem recebida: " + message);

                            if (jsonNode.get("operation").asText().equals("RAISE BID")) {
                                Main.auctionController.updateInfoGame(jsonNode);
                            }

                        }

                    }
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Timeout de 1 minuto atingido, nenhuma mensagem recebida.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Jogo finalizado!");
        Main.auctionController.setAuctionStatus(false);
    }

    @Override
    public void run() {
        listenForMessages();
    }
}
