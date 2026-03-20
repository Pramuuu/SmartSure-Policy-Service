package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PolicyAlreadyActiveException extends RuntimeException {

    public PolicyAlreadyActiveException(Long customerId, Long policyTypeId) {
        super("Customer " + customerId + " already has an active/created policy for type: " + policyTypeId);
    }
}