package stocker.security;

public class AuthenticationHandler {
//    private final String FOLDER_PATH = "src/main/resources/";

    public static void main(String... args) {
        final String FOLDER_PATH = "src/main/resources/";
        final String SECRET_KEY_PATH = FOLDER_PATH + "secret_key.txt";
        final String ENCRYPTED_MESSAGE_PATH = FOLDER_PATH + "encrypted_message.txt";
        Decryptor decryptor = new Decryptor(SECRET_KEY_PATH, ENCRYPTED_MESSAGE_PATH);
        String password = "";
        try {
            password = decryptor.decryptMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String answer = password.equals("jocka123") ? "Authentication successful" : "Authentication failed";

        System.out.println(answer);
    }
}
