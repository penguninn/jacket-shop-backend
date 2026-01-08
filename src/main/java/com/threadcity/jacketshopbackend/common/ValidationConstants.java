package com.threadcity.jacketshopbackend.common;

/**
 * Central location for validation constants used across DTOs.
 * Ensures consistency in validation rules throughout the application.
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // Prevent instantiation
    }

    // Phone number validation
    public static final String PHONE_PATTERN = "^0\\d{9,14}$";
    public static final String PHONE_MESSAGE = "Phone number must start with 0 and have 10-15 digits";
    public static final int PHONE_MAX_LENGTH = 15;

    // Password validation
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";
    public static final String PASSWORD_MESSAGE = "Password must contain at least one uppercase letter, one lowercase letter, and one digit";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;

    // Email validation
    public static final int EMAIL_MAX_LENGTH = 255;

    // Name field sizes
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 150;
    public static final int FULLNAME_MAX_LENGTH = 150;
    public static final int USERNAME_MIN_LENGTH = 6;
    public static final int USERNAME_MAX_LENGTH = 50;

    // Description field sizes
    public static final int DESCRIPTION_SHORT_MAX_LENGTH = 500;
    public static final int DESCRIPTION_LONG_MAX_LENGTH = 4000;

    // Address field sizes
    public static final int ADDRESS_LINE_MAX_LENGTH = 255;
    public static final int RECIPIENT_NAME_MAX_LENGTH = 120;
    public static final int LOCATION_CODE_MAX_LENGTH = 20;
    public static final int LOCATION_NAME_MAX_LENGTH = 100;

    // Product field sizes
    public static final int PRODUCT_NAME_MAX_LENGTH = 200;
    public static final int SKU_MAX_LENGTH = 64;
    public static final int IMAGE_URL_MAX_LENGTH = 500;

    // Coupon/Code field sizes
    public static final int COUPON_CODE_MAX_LENGTH = 50;
    public static final int ORDER_CODE_MAX_LENGTH = 32;

    // Note/Comment field sizes
    public static final int NOTE_MAX_LENGTH = 1000;
    public static final int COMMENT_MAX_LENGTH = 2000;

    // Carrier/Tracking field sizes
    public static final int CARRIER_NAME_MAX_LENGTH = 100;
    public static final int TRACKING_NUMBER_MAX_LENGTH = 100;
    public static final int TRANSACTION_ID_MAX_LENGTH = 255;

    // Hex color validation
    public static final String HEX_COLOR_PATTERN = "^#[0-9A-Fa-f]{6}$";
    public static final String HEX_COLOR_MESSAGE = "Color must be a valid hex code (e.g., #FF5733)";
    public static final int HEX_COLOR_MAX_LENGTH = 10;
}
