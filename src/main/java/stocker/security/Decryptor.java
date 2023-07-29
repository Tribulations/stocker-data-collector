package stocker.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Decryptor {

    private final SecretKey SECRET_KEY;
    private final String ENCRYPTED_MESSAGE;

    public Decryptor(final String secretKeyFilePath, final String encryptedMessageFilePath) {
        try {
            this.SECRET_KEY = readSecretKeyFromFile(secretKeyFilePath);
            this.ENCRYPTED_MESSAGE = readEncryptedMessageFromFile(encryptedMessageFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptMessage() throws Exception {
        Cipher cipher = Cipher.getInstance(SECRET_KEY.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
        byte[] decodedBytes = Base64.getDecoder().decode(ENCRYPTED_MESSAGE);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKey readSecretKeyFromFile(String filePath) throws IOException {
        try (BufferedReader keyReader = new BufferedReader(new FileReader(filePath))) {
            String base64EncodedKey = keyReader.readLine();
            byte[] decodedKey = Base64.getDecoder().decode(base64EncodedKey);
            return new SecretKeySpec(decodedKey, "AES");
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
}
