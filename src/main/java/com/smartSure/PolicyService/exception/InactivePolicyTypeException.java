package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InactivePolicyTypeException extends RuntimeException {

    public InactivePolicyTypeException(String name) {
        super("Policy type '" + name + "' is not currently available for purchase");
    }
}