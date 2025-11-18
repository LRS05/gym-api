package com.project.gym.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String message) {
        super(message);
    }
}
