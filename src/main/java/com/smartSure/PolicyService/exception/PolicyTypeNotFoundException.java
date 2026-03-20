package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PolicyTypeNotFoundException extends RuntimeException {

    public PolicyTypeNotFoundException(Long id) {
        super("Policy type not found with id: " + id);
    }
}