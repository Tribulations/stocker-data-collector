package stocker.security;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileEncryptor {

    private SecretKey secretKey;

    private void generateSecretKey(String algorithm, int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(keySize);
        secretKey = keyGen.generateKey();
    }

    public String encryptMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decryptMessage(String encryptedMessage) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private void writeSecretKeyToFile(String filePath) throws IOException {
        try (FileWriter keyWriter = new FileWriter(filePath)) {
            byte[] encodedKey = secretKey.getEncoded();
            String base64EncodedKey = Base64.getEncoder().encodeToString(encodedKey);
            keyWriter.write(base64EncodedKey);
        }
    }

    private void writeEncryptedMessageToFile(String filePath, String encryptedMessage) throws IOException {
        try (FileWriter encryptedWriter = new FileWriter(filePath)) {
            encryptedWriter.write(encryptedMessage);
        }
    }

    private void readSecretKeyFromFile(String filePath) throws IOException {
        try (BufferedReader keyReader = new BufferedReader(new FileReader(filePath))) {
            String base64EncodedKey = keyReader.readLine();
            byte[] decodedKey = Base64.getDecoder().decode(base64EncodedKey);
            secretKey = new SecretKeySpec(decodedKey, "AES");
        }
    }

    private String readEncryptedMessageFromFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader encryptedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = encryptedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        try {
            FileEncryptor encryptor = new FileEncryptor();

            // Step 1: Generate the secret key
            encryptor.generateSecretKey("AES", 256);

            // Step 2: Encrypt the message
            String message = "j";
            String encryptedMessage = encryptor.encryptMessage(message);

            // Step 3: Write the secret key and encrypted message to files
            final String folderPath = "src/main/resources/";
            encryptor.writeSecretKeyToFile(folderPath + "username_secret_key.log");
            encryptor.writeEncryptedMessageToFile(folderPath + "encrypted_username.log", encryptedMessage);

            // Step 4: Read the secret key and encrypted message from files
            encryptor.readSecretKeyFromFile(folderPath + "username_secret_key.log");
            String encryptedMessageFromFile = encryptor.readEncryptedMessageFromFile(folderPath + "encrypted_username.log");

            // Step 5: Decrypt the message
            String decryptedMessage = encryptor.decryptMessage(encryptedMessageFromFile);

            System.out.println("Original Message: " + message);
            System.out.println("Decrypted Message: " + decryptedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
