package com.bmglewis.exception;

/**
 * FILE TYPE: CLASS (Exception)
 * Exception thrown when trying to create a duplicate resource
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}