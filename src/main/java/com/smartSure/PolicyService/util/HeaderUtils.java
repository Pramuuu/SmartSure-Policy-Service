
package com.smartSure.PolicyService.util;

import jakarta.servlet.http.HttpServletRequest;
import feign.RequestTemplate;

public class HeaderUtils {

    public static void copyHeaders(HttpServletRequest request, RequestTemplate template) {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String secret = request.getHeader("X-Internal-Secret");

        if (userId != null) {
            template.header("X-User-Id", userId);
        }

        if (role != null) {
            template.header("X-User-Role", role);
        }

        if (secret != null) {
            template.header("X-Internal-Secret", secret);
        }


    }
}