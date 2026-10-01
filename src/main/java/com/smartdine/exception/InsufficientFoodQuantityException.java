package com.smartdine.exception;

public class InsufficientFoodQuantityException extends RuntimeException {
    public InsufficientFoodQuantityException(String message) {
        super(message);
    }

    public InsufficientFoodQuantityException(String foodName, int requested, int remaining) {
        super("Insufficient quantity for '" + foodName + "'. Requested: " + requested + ", Remaining available: " + remaining);
    }
}
