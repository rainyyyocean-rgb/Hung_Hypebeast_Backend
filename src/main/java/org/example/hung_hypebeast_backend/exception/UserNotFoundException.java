package org.example.hung_hypebeast_backend.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String username, boolean byUsername) {
        super("Không tìm thấy người dùng: " + username);
    }
}

