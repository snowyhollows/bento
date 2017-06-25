package net.snowyhollows.bento2;

/**
 * Created by fdreger on 6/25/2017.
 */
public class BentoException extends RuntimeException {
    public BentoException() {
    }

    public BentoException(String message) {
        super(message);
    }
}
