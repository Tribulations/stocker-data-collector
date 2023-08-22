package stocker.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.security.NoSuchAlgorithmException;

import java.util.Base64;

/**
 * Class used to encrypt a password or username etc.
 */
public class FileEncryptor {
    private SecretKey secretKey;

    /**
     * Initialized member field and creates two files where one contains the message and one the corresponding
     * secret key to decrypt the message.
     * @param messageToEncryptAndAddToFile message to encrypt
     * @param secretKeyFilepath the file path where the secret key corresponding to the encrypted message should be stored
     * @param encryptedMessageFilepath the file path to where the encrypted message should be stored
     */
    private FileEncryptor(final String messageToEncryptAndAddToFile,
                          final String secretKeyFilepath,
                          final String encryptedMessageFilepath) {
        try {
            this.secretKey = generateSecretKey();
            final String encryptedMessage = encryptMessage(messageToEncryptAndAddToFile);
            final String folderPath = "src/main/resources/";
            writeSecretKeyToFile(folderPath + secretKeyFilepath);
            writeEncryptedMessageToFile(folderPath + encryptedMessageFilepath, encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public String encryptMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
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

    /**
     * Used to create two files storing the usernames secret key and encrypted username.
     * @param message the username to encrypt
     * @param secretKeyFilepath the file path to the file where the secret key for the username should be stored
     * @param encryptedMessageFilepath file path to file where the encrypted username should be stored
     */
    private static void createEncryptedUsernameFile(final String message, final String secretKeyFilepath,
                                                   final String encryptedMessageFilepath) {
        FileEncryptor usernameEncryptor = new FileEncryptor(message, secretKeyFilepath, encryptedMessageFilepath);
    }

    private static void createEncryptedPasswordFile(final String message, final String secretKeyFilepath,
                                                    final String encryptedMessageFilepath) {
        FileEncryptor passwordEncryptor = new FileEncryptor(message, secretKeyFilepath, encryptedMessageFilepath);
    }
    public static void main(String... args) {
//        createEncryptedPasswordFile();
//        createEncryptedUsernameFile();
    }
}
