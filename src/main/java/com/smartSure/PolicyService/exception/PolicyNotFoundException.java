package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(Long id) {
        super("Policy not found with id: " + id);
    }

    public PolicyNotFoundException(String policyNumber) {
        super("Policy not found with number: " + policyNumber);
    }
}