package com.adorsky.applications.sample.exception;

/**
 * Sample Command Line Bank Account - Jaroop Application
 * @author: adorsky
 */

public class MalformedMenuSelectionException extends Exception {

    public MalformedMenuSelectionException(String message) {
        super(message);
    }
}
