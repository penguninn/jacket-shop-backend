package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CartValidationResponse {
    private boolean isValid;
    private List<CartIssue> issues;

    @Data
    @Builder
    public static class CartIssue {
        private Long productVariantId;
        private String productName;
        private String issueType;
        private String message;
    }
}
