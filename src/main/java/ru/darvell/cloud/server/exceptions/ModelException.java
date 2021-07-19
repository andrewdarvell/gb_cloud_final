package ru.darvell.cloud.server.exceptions;

public class ModelException extends RuntimeException{

    public ModelException(String message) {
        super(message);
    }

    protected ModelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
