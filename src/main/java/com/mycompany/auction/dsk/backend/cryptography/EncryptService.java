package com.mycompany.auction.dsk.backend.cryptography;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptService {

    private final String certificatesDir = System.getProperty("user.dir");
    private MessageDigest md;
    private Cipher cipher;

    public EncryptService() {
        try {
            this.md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isClientRegistered(File clientCertificateName) {
        return clientCertificateName.exists() && clientCertificateName.isFile();
    }

    /*
    TO DO:
    Criar método para encriptografar
    Criar método para decriptografar
    Criar método para assinar
     */
    public String encrypt(String message, String CPF)  {
        try {
            String completeMessage = addSecurityInfoInTheMessage(message);
            
            System.out.println("\nMensagem antes de encriptar: " + completeMessage);
            PublicKey clientPK = getClientPublicKey(CPF);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clientPK);
            byte[] encryptedBytes = cipher.doFinal(completeMessage.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (JsonProcessingException | NoSuchAlgorithmException 
                | NoSuchPaddingException | InvalidKeyException 
                | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "";
    }

    public void decrypt() {

    }

    public PublicKey getClientPublicKey(String CPF) {

        File jsonFile = new File(this.certificatesDir + "\\clients\\" + CPF + ".json");

        if (isClientRegistered(jsonFile)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonFile); // Lê o arquivo JSON

                JsonNode publicKeyNode = rootNode.get("public-key");
                if (publicKeyNode != null) {
                    String publicKeyBase64 = publicKeyNode.asText();

                    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                    return keyFactory.generatePublic(keySpec); // Retorna a chave pública
                } else {
                    System.out.println("Campo 'public-key' não encontrado no arquivo.");
                }
            } catch (IOException | java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
                System.out.println("Erro ao ler ou converter a chave pública: " + e.getMessage());
            }
        }

        return null;
    }

    public SecretKey getSymmetricKey() {
        File jsonFile = new File(certificatesDir + "\\" + "symmetricKey.json");

        try {
            if (jsonFile.exists()) {
                byte[] jsonData = Files.readAllBytes(jsonFile.toPath());

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonData);

                String encodedKey = jsonNode.get("public-key").asText();

                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

                SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                return secretKey;
            } else {
                System.out.println("Arquivo não encontrado: " + jsonFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String signMessage(String message) {
        byte[] hashBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);

            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public String addSecurityInfoInTheMessage(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(message);
        ObjectNode objectNode = (ObjectNode) jsonNode;

        SecretKey symmetricKey = getSymmetricKey();

        byte[] encodedKey = symmetricKey.getEncoded(); // Pega os bytes da chave secreta

        String messageHash = signMessage(message);
        String symmetricKeyString = Base64.getEncoder().encodeToString(encodedKey);

        objectNode.put("symmetric-key", symmetricKeyString);
        objectNode.put("hash", messageHash);
        System.out.println("Mensagem completa: " + objectNode.toString());
        
        return objectNode.toString();
    }
}
