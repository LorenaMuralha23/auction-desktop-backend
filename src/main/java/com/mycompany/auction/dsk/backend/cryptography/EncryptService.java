package com.mycompany.auction.dsk.backend.cryptography;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

    public String encryptAssymmetric(String message, String CPF) {
        try {
            PublicKey clientPK = getClientPublicKey(CPF);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clientPK);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public void decryptAssymetric() {

    }

    public String encryptSymmetric(String message) {
        try {
            byte[] iv = "1234567890123456".getBytes(StandardCharsets.UTF_8); // Exemplo de IV fixo
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            SecretKey serverSymmKey = getSymmetricKey();

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, serverSymmKey, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public String decryptSymmetric(String encryptedMessage) {
        try {
            byte[] iv = "1234567890123456".getBytes(StandardCharsets.UTF_8);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            SecretKey serverSymmKey = getSymmetricKey();

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, serverSymmKey, ivParameterSpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public PublicKey getClientPublicKey(String CPF) {

        File jsonFile = new File(this.certificatesDir + "\\clients\\" + CPF + ".json");

        if (isClientRegistered(jsonFile)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonFile);

                JsonNode publicKeyNode = rootNode.get("public-key");
                if (publicKeyNode != null) {
                    String publicKeyBase64 = publicKeyNode.asText();

                    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                    return keyFactory.generatePublic(keySpec);
                } else {
                    System.out.println("Campo 'public-key' não encontrado no arquivo.");
                }
            } catch (IOException | java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
                System.out.println("Erro ao ler ou converter a chave pública: " + e.getMessage());
            }
        }

        return null;
    }

    public PrivateKey getPrivateKey() {
        File jsonFile = new File(this.certificatesDir + "\\server.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            JsonNode privateKeyNode = rootNode.get("private-key");
            if (privateKeyNode != null) {
                String privateKeyBs64 = privateKeyNode.asText();

                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBs64);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                return keyFactory.generatePrivate(keySpec);
            } else {
                System.out.println("Campo 'private-key' não encontrado no arquivo.");
            }
        } catch (IOException | java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            System.out.println("Erro ao ler ou converter a chave private: " + e.getMessage());
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

    public byte[] calculateHash(String message) {
        byte[] hashBytes = md.digest(message.getBytes(StandardCharsets.UTF_8));
        return hashBytes;
    }

    public byte[] signHash(byte[] hashBytes) {
        try {
            PrivateKey serverPk = getPrivateKey();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(serverPk);
            signature.update(hashBytes);
            return signature.sign(); 
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Logger.getLogger(EncryptService.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

}
