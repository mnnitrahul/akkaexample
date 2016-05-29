package Client.exception;

/**
 * Created by rahul.ka on 20/05/16.
 */
public class ClientNotInitializedException extends RuntimeException {

    public ClientNotInitializedException(String message){
        super(message);
    }

    public ClientNotInitializedException(String message, Throwable cause){
        super(message, cause);
    }
}
