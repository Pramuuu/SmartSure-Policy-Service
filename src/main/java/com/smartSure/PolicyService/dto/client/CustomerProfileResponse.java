package com.smartSure.PolicyService.dto.client;

import lombok.*;

/**
 * Response DTO for customer data fetched from AuthService.
 * Only the fields PolicyService needs — not the full user object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse {

    private Long   id;
    private String name;
    private String email;
    private String phone;
    private Integer age;
}
