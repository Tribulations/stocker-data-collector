package stocker.security;

/**
 * Class used to handle authentication to the database etc.
 * @author tribulations
 * @version 1.0
 * @since 1.0
 */
public class Authenticator { // todo improvenRefsctor !! security has to be refactoring
    public static final Authenticator INSTANCE = new Authenticator();
    public final String FOLDER_PATH = "src/main/resources/"; // todo use the Path class instead to create paths!!
    public final String USERNAME_SECRET_KEY_PATH = FOLDER_PATH + "username_secret_key.log";
    public final String PASSWORD_SECRET_KEY_PATH = FOLDER_PATH + "password_secret_key.log";
    public final String USERNAME_ENCRYPTED_PATH = FOLDER_PATH + "encrypted_username.log";
    public final String PASSWORD_ENCRYPTED_PATH = FOLDER_PATH + "encrypted_password.log";

    private final Decryptor username;
    private final Decryptor password;

    private Authenticator() {
        this.username = new Decryptor(USERNAME_SECRET_KEY_PATH, USERNAME_ENCRYPTED_PATH);
        this.password = new Decryptor(PASSWORD_SECRET_KEY_PATH, PASSWORD_ENCRYPTED_PATH);
    }

    /**
     * Accessor to the database password.
     * @return the database password
     */
    public String getDbPassword() {
        return password.decrypt();
    }

    /**
     * Accessor to the database username.
     * @return the database username
     */
    public String getDbUsername() {
        return username.decrypt();
    }

    public String getDecryptedMessage(final String secretKeyFilePath, final String encryptedMessageFilePath) {
        Decryptor decryptor = new Decryptor(secretKeyFilePath, encryptedMessageFilePath);
        return decryptor.decrypt();
    }

    /**
     * used for testing.
     * @param args not used here
     */
    public static void main(String... args) {
        System.out.println(Authenticator.INSTANCE.getDbPassword());
        System.out.println(Authenticator.INSTANCE.getDbUsername());
    }
}
