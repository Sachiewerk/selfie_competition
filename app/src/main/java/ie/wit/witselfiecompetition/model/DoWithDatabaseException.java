package ie.wit.witselfiecompetition.model;

/**
 * Custom Exception for DoWithData Class
 * Created by Yahya Almardeny on 13/03/18.
 */

public class DoWithDatabaseException extends Exception {

    public DoWithDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
