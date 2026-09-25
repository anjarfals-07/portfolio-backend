package com.anjar.portfolio.exception;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String message) {
        super(message);
    }

    public SlugAlreadyExistsException(String slug, String reason) {
        super("Slug '" + slug + "' " + reason);
    }
}