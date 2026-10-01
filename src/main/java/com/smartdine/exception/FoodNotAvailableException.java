package com.smartdine.exception;

public class FoodNotAvailableException extends RuntimeException {
    public FoodNotAvailableException(String message) {
        super(message);
    }

    public FoodNotAvailableException(Long foodId, String foodName) {
        super("Food item '" + foodName + "' (ID: " + foodId + ") is currently unavailable or sold out.");
    }
}
