package com.project.gym.exception;

public class UserAlreadyAuthenticatedException extends RuntimeException {
    public UserAlreadyAuthenticatedException(String message) {
        super(message);
    }
}
