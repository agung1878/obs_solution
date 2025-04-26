package com.obs.example.exception;

import lombok.Getter;

@Getter
public class ReferentialIntegrityViolationException extends RuntimeException {
    public ReferentialIntegrityViolationException(String message) {
        super(message);
    }

}
