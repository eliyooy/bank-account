package com.adorsky.applications.sample.exception;

/**
 * Thrown when an invalid number is entered into the command line application
 * @author: adorsky
 */
public class InvalidNumberException extends Exception {

    public InvalidNumberException(final String message) {
        super(message);
    }
}
