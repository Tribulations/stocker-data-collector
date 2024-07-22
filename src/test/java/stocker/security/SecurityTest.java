package stocker.security;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is used to test the classes in the security package.
 * These classes are: {@link stocker.security.Authenticator}, {@link stocker.security.Decryptor}, and
 * {@link stocker.security.FileEncryptor}
 *
 *
 * @version 1.0
 * @author Joakim Colloz
 */
class SecurityTest {
    private final String FOLDER_PATH = "src/main/resources/"; // todo use the Path class instead to create paths!!
    private final String TEST_SECRET_KEY_PATH = FOLDER_PATH + "test_secret_key.log";
    private final String TEST_ENCRYPTED_PATH = FOLDER_PATH + "encrypted_test.log";
    private final String message = "hidden_message";

    /**
     * Verifies the creation of files for the encrypted message and its corresponding secret key.
     * <p>
     * This test ensures that the {@link FileEncryptor#createEncryptedFiles(String, String, String)}
     * method creates the specified files for the encrypted message and the secret key if they do not
     * already exist. It checks that the following files are successfully created:
     * <ul>
     *     <li>The file located at <code>TEST_SECRET_KEY_PATH</code>, which is intended to store the base64-encoded secret key.</li>
     *     <li>The file located at <code>TEST_ENCRYPTED_PATH</code>, which is intended to store the encrypted message.</li>
     * </ul>
     * The test will pass if both files are found to exist after the method call, confirming that the
     * file creation operation has been executed as expected.
     * <p>
     * The test uses the following file paths:
     * <ul>
     *     <li><code>TEST_SECRET_KEY_PATH</code>: The file path where the secret key should be saved.</li>
     *     <li><code>TEST_ENCRYPTED_PATH</code>: The file path where the encrypted message should be saved.</li>
     * </ul>
     *
     * @see FileEncryptor#createEncryptedFiles(String, String, String)
     */
    @Test
    void verifyCreationOfEncryptedMessageAndKeyFiles() {
        FileEncryptor.createEncryptedFiles(message, TEST_SECRET_KEY_PATH, TEST_ENCRYPTED_PATH);

        assertTrue(filesExist(TEST_SECRET_KEY_PATH));
        assertTrue(filesExist(TEST_ENCRYPTED_PATH));
    }

    /**
     * Verifies that the {@link Authenticator} correctly decrypts a message using the specified
     * secret key and encrypted message files.
     * <p>
     * This test ensures that the decryption functionality provided by the {@link Authenticator}
     * is working as expected by comparing the decrypted message to the original message.
     * </p>
     *
     * @see Authenticator#getDecryptedMessage(String, String)
     */
    @Test
    void shouldDecryptMessageWithAuthenticator() {
        assertEquals(message, Authenticator.INSTANCE.getDecryptedMessage(TEST_SECRET_KEY_PATH, TEST_ENCRYPTED_PATH));
    }

    /**
     * Verifies that a {@link RuntimeException} is thrown when an invalid path to the secret key is used
     * with the {@link Authenticator} to decrypt a message.
     * <p>
     * This test ensures that the system properly handles errors related to invalid secret key files.
     * </p>
     *
     * @see Authenticator#getDecryptedMessage(String, String)
     */
    @Test
    void shouldThrowExceptionForInvalidSecretKeyPath() {
        assertThrows(RuntimeException.class, () -> {
            Authenticator.INSTANCE.getDecryptedMessage(
                    "wrong_path_to_secret", TEST_ENCRYPTED_PATH);
        });
    }

    private boolean filesExist(String filePath) {
        return new File(filePath).exists();
    }
}