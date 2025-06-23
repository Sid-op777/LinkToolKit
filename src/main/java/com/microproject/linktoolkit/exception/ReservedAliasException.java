package com.microproject.linktoolkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Maps to a 400 status code
public class ReservedAliasException extends RuntimeException {
    public ReservedAliasException(String message) {
        super(message);
    }
}