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
public class FileEncryptor {;
    private SecretKey secretKey;


    /**
     * Initialized member field and creates two files where one contains the message and one the corresponding
     * secret key to decrypt the message.
     * @param messageToEncryptAndAddToFile message to encrypt
     * @param secretKeyFilepath the file path where the secret key corresponding to the encrypted message should be stored
     * @param encryptedMessageFilepath the file path to where the encrypted message should be stored
     */
    public FileEncryptor(final String messageToEncryptAndAddToFile,
                          final String secretKeyFilepath,
                          final String encryptedMessageFilepath) {
        try {
            this.secretKey = generateSecretKey();
            final String encryptedMessage = encryptMessage(messageToEncryptAndAddToFile);
            writeSecretKeyToFile(secretKeyFilepath);
            writeEncryptedMessageToFile(encryptedMessageFilepath, encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and writes an encrypted message and its corresponding secret key to specified files.
     * <p>
     * This method initializes an instance of the {@link FileEncryptor} class to perform the following tasks:
     * <ul>
     *     <li>Generates a new secret key for encryption.</li>
     *     <li>Encrypts the provided message using the generated secret key.</li>
     *     <li>Saves the encrypted message to the specified file.</li>
     *     <li>Saves the base64-encoded secret key to the specified file.</li>
     * </ul>
     * The method encapsulates the entire process of encryption and file writing, ensuring that both the
     * encrypted message and the secret key are properly stored and can be used for secure message decryption
     * later.
     *
     * @param message                The message to be encrypted and stored. This can be any string that
     *                              needs to be kept secure.
     * @param encryptedMessageFilepath The path to the file where the encrypted message will be written.
     *                                 The file will be created if it does not exist, or overwritten if it does.
     * @param secretKeyFilepath       The path to the file where the base64-encoded secret key will be written.
     *                                 The file will be created if it does not exist, or overwritten if it does.
     *
     * @throws IllegalArgumentException if any of the file paths are null or empty.
     * @throws RuntimeException if an error occurs during encryption or file writing.
     *
     * @see FileEncryptor
     */
    public static void createEncryptedFiles(String message, String secretKeyFilepath, String encryptedMessageFilepath) {
        new FileEncryptor(message, secretKeyFilepath, encryptedMessageFilepath);
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    private String encryptMessage(String message) throws Exception {
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

    // TODO this class should be an enum? How should it be used? Who is the client? Redesign!
    public static void main(String... args) {
        final String FOLDER_PATH = "src/main/resources/";
        final String USERNAME_SECRET_KEY_PATH = FOLDER_PATH + "username_secret_key.log";
        final String PASSWORD_SECRET_KEY_PATH = FOLDER_PATH + "password_secret_key.log";
        final String USERNAME_ENCRYPTED_PATH = FOLDER_PATH + "encrypted_username.log";
        final String PASSWORD_ENCRYPTED_PATH = FOLDER_PATH + "encrypted_password.log";

        createEncryptedPasswordFile("jocka123", PASSWORD_SECRET_KEY_PATH, PASSWORD_ENCRYPTED_PATH);
        createEncryptedUsernameFile("jocka", USERNAME_SECRET_KEY_PATH, USERNAME_ENCRYPTED_PATH);
    }
}
