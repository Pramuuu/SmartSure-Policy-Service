package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CoverageExceedsLimitException extends RuntimeException {

    public CoverageExceedsLimitException(BigDecimal requested, BigDecimal max) {
        super("Requested coverage " + requested + " exceeds maximum allowed coverage of " + max);
    }
}