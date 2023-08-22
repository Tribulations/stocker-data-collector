package stocker.security;

import stocker.support.StockAppLogger;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

/**
 * Class used to decrypt a username and its corresponding password. Only used within the {@link Authenticator} class.
 * @author tribulations
 * @version 1.0
 * @since 1.0
 */
public class Decryptor {

    private final SecretKey SECRET_KEY;
    private final String ENCRYPTED_MESSAGE;

    /**
     * Constructor initializing member fields by calling internal helper methods.
     * @param secretKeyFilePath path to the file storing the secret key
     * @param encryptedMessageFilePath path to the file storing the encrypted message
     */
    public Decryptor(final String secretKeyFilePath, final String encryptedMessageFilePath) {
        try {
            this.SECRET_KEY = readSecretKeyFromFile(secretKeyFilePath);
            this.ENCRYPTED_MESSAGE = readEncryptedMessageFromFile(encryptedMessageFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Decrypts the encrypted message using the secret key and the encrypted message in the files located at
     * the filepaths passed at arguments to the class constructor upon instantiation of a Decryptor object,
     * which only should be done from within the Authenticator class.
     * @return the decrypted message
     */
    public String decrypt() {
        Cipher cipher = null;
        byte[] decryptedBytes = new byte[0];
        try {
            cipher = Cipher.getInstance(SECRET_KEY.getAlgorithm());

            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);

            byte[] decodedBytes = Base64.getDecoder().decode(ENCRYPTED_MESSAGE);
            decryptedBytes = new byte[0];

            decryptedBytes = cipher.doFinal(decodedBytes);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Called internally from the method class constructor.
     * @param filePath file path to the file storing the secret key
     * @return the secret key as a SecretKey object
     * @throws IOException when the file at the filepath can't be opened
     */
    private SecretKey readSecretKeyFromFile(String filePath) throws IOException {
        try (BufferedReader keyReader = new BufferedReader(new FileReader(filePath))) {
            String base64EncodedKey = keyReader.readLine();
            byte[] decodedKey = Base64.getDecoder().decode(base64EncodedKey);
            return new SecretKeySpec(decodedKey, "AES");
        }
    }

    /**
     * Called internally from the method class constructor.
     * @param filePath file path to the file storing the secret key
     * @return the encrypted message string
     * @throws IOException when the file at the filepath can't be opened
     */
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
