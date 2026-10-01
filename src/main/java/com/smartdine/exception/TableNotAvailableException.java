package com.smartdine.exception;

public class TableNotAvailableException extends RuntimeException {
    public TableNotAvailableException(String message) {
        super(message);
    }

    public TableNotAvailableException(Integer tableNumber, Long establishmentId) {
        super("Table #" + tableNumber + " at establishment " + establishmentId + " is currently occupied or unavailable.");
    }
}
