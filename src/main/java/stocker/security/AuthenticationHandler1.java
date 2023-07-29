package stocker.security;

import stocker.support.StockAppLogger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthenticationHandler1 {
    private static final String ALGORITHM = "AES";
    public static final AuthenticationHandler1 INSTANCE = new AuthenticationHandler1();

    public static void main(String[] args) throws Exception {
//        test();
        AuthenticationHandler1.INSTANCE.writeToFile(AuthenticationHandler1.generateAESKey().getEncoded().toString());
    }

    private static void test() throws Exception {
        // Generate a random AES key
        SecretKey secretKey = generateAESKey();

        String plainText = "Hello, this is a test message!";
        System.out.println("Original Message: " + plainText);

        // Encrypt the message
        String encryptedText = encrypt(plainText, secretKey);
        System.out.println("Encrypted Message: " + encryptedText);

        // Decrypt the message
        String decryptedText = decrypt(encryptedText, secretKey);
        System.out.println("Decrypted Message: " + decryptedText);
    }

    protected void writeToFile(final String text) {
        try {
            String fileName = "sec.log";
            FileWriter fileWriter = new FileWriter("src/main/resources/" + fileName, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(text);

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            StockAppLogger.INSTANCE.logDebug(e.getMessage());
        }
    }

    // Generate a random AES key
    private static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(192); // Use 128, 192, or 256 bits key size for AES
        return keyGen.generateKey();
    }

    // Encrypt using AES
    private static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt using AES
    private static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

