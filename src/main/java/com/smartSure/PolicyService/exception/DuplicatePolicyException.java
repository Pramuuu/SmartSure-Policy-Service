package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePolicyException extends RuntimeException {

    public DuplicatePolicyException() {
        super("An active policy of this type already exists for this customer");
    }
}