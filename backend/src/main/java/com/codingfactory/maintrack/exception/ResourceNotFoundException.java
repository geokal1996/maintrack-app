package com.codingfactory.maintrack.exception;

// Tin "petame" otan psaxnoume kati (p.x. mihani me id=5) kai den yparxei.
// O GlobalExceptionHandler tin piadnei kai epistrefei sto client kathara 404, oxi krash.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
