package stocker.support;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AuthenticationHandler2 {

    private SecretKey secretKey;

    public void generateSecretKey(String algorithm, int keySize) throws NoSuchAlgorithmException {
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

    public void writeSecretKeyToFile(String filePath) throws IOException {
        try (FileWriter keyWriter = new FileWriter(filePath)) {
            byte[] encodedKey = secretKey.getEncoded();
            String base64EncodedKey = Base64.getEncoder().encodeToString(encodedKey);
            keyWriter.write(base64EncodedKey);
        }
    }

    public void writeEncryptedMessageToFile(String filePath, String encryptedMessage) throws IOException {
        try (FileWriter encryptedWriter = new FileWriter(filePath)) {
            encryptedWriter.write(encryptedMessage);
        }
    }

    public static void main(String[] args) {
        try {
            AuthenticationHandler2 encryptor = new AuthenticationHandler2();

            // Step 1: Generate the secret key
            encryptor.generateSecretKey("AES", 256);

            // Step 2: Encrypt the message
            String message = "This is the message to be encrypted.";
            String encryptedMessage = encryptor.encryptMessage(message);

            // Step 3: Write the secret key and encrypted message to files
            encryptor.writeSecretKeyToFile("secret_key.txt");
            encryptor.writeEncryptedMessageToFile("encrypted_message.txt", encryptedMessage);

            System.out.println("Encryption and writing to files successful.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
