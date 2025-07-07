package se450.assignment2.exception;

public class InvalidQuoteException extends Exception {
    public InvalidQuoteException(String msg) {
        super(msg);
    }

    public InvalidQuoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
