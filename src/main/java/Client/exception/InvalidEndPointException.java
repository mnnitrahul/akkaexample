package Client.exception;

/**
 * Created by rahul.ka on 20/05/16.
 */
public class InvalidEndPointException  extends RuntimeException {

    public InvalidEndPointException(String message){
        super(message);
    }

    public InvalidEndPointException(String message, Throwable cause){
        super(message, cause);
    }
}
