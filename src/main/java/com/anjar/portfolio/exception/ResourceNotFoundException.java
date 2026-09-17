package com.anjar.portfolio.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " dengan id " + id + " tidak ditemukan");
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(resource + " dengan " + field + " '" + value + "' tidak ditemukan");
    }
}