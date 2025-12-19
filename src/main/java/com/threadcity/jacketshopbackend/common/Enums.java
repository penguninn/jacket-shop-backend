package com.threadcity.jacketshopbackend.common;

public class Enums {

    public enum Status {
        ACTIVE, INACTIVE
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED, RETURNED
    }

    public enum PaymentStatus {
        UNPAID, PAID, REFUNDED
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
}
