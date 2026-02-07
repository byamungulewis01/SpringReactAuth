package com.bmglewis.exception;

/**
 * FILE TYPE: CLASS (Exception)
 * Thrown when token validation or processing fails
 */
public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}