package com.emtech.loanapp.Configurations.Utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceAbsentException extends RuntimeException {
    public ResourceAbsentException(String message) {
        super(message);
    }
}

