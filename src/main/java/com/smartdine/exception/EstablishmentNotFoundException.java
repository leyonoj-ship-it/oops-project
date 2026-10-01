package com.smartdine.exception;

public class EstablishmentNotFoundException extends RuntimeException {
    public EstablishmentNotFoundException(String message) {
        super(message);
    }

    public EstablishmentNotFoundException(Long establishmentId) {
        super("Establishment (Hotel/Canteen) not found with ID: " + establishmentId);
    }
}
