package com.smartSure.PolicyService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PremiumNotFoundException extends RuntimeException {

    public PremiumNotFoundException(Long premiumId, Long policyId) {
        super("Premium " + premiumId + " not found for policy " + policyId);
    }
}