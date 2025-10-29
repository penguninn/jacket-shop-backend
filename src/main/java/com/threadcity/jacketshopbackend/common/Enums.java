package com.threadcity.jacketshopbackend.common;

public class Enums {

    public enum Status {
        ACTIVE, INACTIVE
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }

    public enum TokenType {
        ACCESS, REFRESH
    }

    public enum RefreshTokenStatus {
        ACTIVE, REVOKED, EXPIRED
    }

    public enum CouponType {
        PERCENT, AMOUNT
    }

    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED
    }
}
