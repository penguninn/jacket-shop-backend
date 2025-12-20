package com.threadcity.jacketshopbackend.exception;

/**
 * Centralized Error Codes following pattern: DOMAIN_ACTION_RESULT
 */
public final class ErrorCodes {

    // User Domain
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_EMAIL_DUPLICATE = "USER_EMAIL_DUPLICATE";
    public static final String USER_USERNAME_DUPLICATE = "USER_USERNAME_DUPLICATE";
    public static final String USER_UPDATE_FAILED = "USER_UPDATE_FAILED";
    public static final String USER_BULK_DELETE_PARTIAL = "USER_BULK_DELETE_PARTIAL";
    public static final String USER_SELF_DELETE = "USER_SELF_DELETE";

    // Product Domain
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_NAME_DUPLICATE = "PRODUCT_NAME_DUPLICATE";
    public static final String PRODUCT_INVALID_QUANTITY = "PRODUCT_INVALID_QUANTITY";
    public static final String PRODUCT_OUT_OF_STOCK = "PRODUCT_OUT_OF_STOCK";
    public static final String PRODUCT_VARIANT_NOT_FOUND = "PRODUCT_VARIANT_NOT_FOUND";
    public static final String PRODUCT_VARIANT_SKU_DUPLICATE = "PRODUCT_VARIANT_SKU_DUPLICATE";
    public static final String PRODUCT_VARIANT_DUPLICATE = "PRODUCT_VARIANT_DUPLICATE";

    // Address Domain
    public static final String ADDRESS_NOT_FOUND = "ADDRESS_NOT_FOUND";
    public static final String ADDRESS_OWNERSHIP_VIOLATION = "ADDRESS_OWNERSHIP_VIOLATION";
    public static final String ADDRESS_ALREADY_DEFAULT = "ADDRESS_ALREADY_DEFAULT";
    public static final String ADDRESS_CANNOT_UNSET_DEFAULT = "ADDRESS_CANNOT_UNSET_DEFAULT";
    public static final String ADDRESS_SET_ANOTHER_DEFAULT = "ADDRESS_SET_ANOTHER_DEFAULT";
    public static final String ADDRESS_DELETE_DEFAULT = "ADDRESS_DELETE_DEFAULT";
    public static final String ADDRESS_DELETE_LAST_DEFAULT = "ADDRESS_DELETE_LAST_DEFAULT";
    public static final String ADDRESS_INVALID_HIERARCHY = "ADDRESS_INVALID_HIERARCHY";

    // Cart Domain
    public static final String CART_ITEM_NOT_FOUND = "CART_ITEM_NOT_FOUND";

    // Auth Domain
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
    public static final String AUTH_REFRESH_TOKEN_INVALID = "AUTH_REFRESH_TOKEN_INVALID";
    public static final String AUTH_BAD_CREDENTIALS = "AUTH_BAD_CREDENTIALS";

    // Role Domain
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";

    // Cloudinary / External
    public static final String CLOUDINARY_UPLOAD_FAILED = "CLOUDINARY_UPLOAD_FAILED";
    public static final String CLOUDINARY_DELETE_FAILED = "CLOUDINARY_DELETE_FAILED";
    public static final String CLOUDINARY_INVALID_FILE = "CLOUDINARY_INVALID_FILE";

    // Brand Domain
    public static final String BRAND_NOT_FOUND = "BRAND_NOT_FOUND";
    public static final String BRAND_NAME_DUPLICATE = "BRAND_NAME_DUPLICATE";

    // Category Domain
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String CATEGORY_NAME_DUPLICATE = "CATEGORY_NAME_DUPLICATE";
    public static final String CATEGORY_PARENT_NOT_FOUND = "CATEGORY_PARENT_NOT_FOUND";

    // Material Domain
    public static final String MATERIAL_NOT_FOUND = "MATERIAL_NOT_FOUND";
    public static final String MATERIAL_NAME_DUPLICATE = "MATERIAL_NAME_DUPLICATE";

    // Size Domain
    public static final String SIZE_NOT_FOUND = "SIZE_NOT_FOUND";
    public static final String SIZE_NAME_DUPLICATE = "SIZE_NAME_DUPLICATE";

    // Style Domain
    public static final String STYLE_NOT_FOUND = "STYLE_NOT_FOUND";
    public static final String STYLE_NAME_DUPLICATE = "STYLE_NAME_DUPLICATE";

    // Color Domain
    public static final String COLOR_NOT_FOUND = "COLOR_NOT_FOUND";
    public static final String COLOR_NAME_DUPLICATE = "COLOR_NAME_DUPLICATE";

    // Coupon Domain
    public static final String COUPON_NOT_FOUND = "COUPON_NOT_FOUND";
    public static final String COUPON_CODE_DUPLICATE = "COUPON_CODE_DUPLICATE";

    // Payment Method Domain
    public static final String PAYMENT_METHOD_NOT_FOUND = "PAYMENT_METHOD_NOT_FOUND";
    public static final String PAYMENT_METHOD_NAME_DUPLICATE = "PAYMENT_METHOD_NAME_DUPLICATE";

    // Shipping Method Domain
    public static final String SHIPPING_METHOD_NOT_FOUND = "SHIPPING_METHOD_NOT_FOUND";
    public static final String SHIPPING_METHOD_NAME_DUPLICATE = "SHIPPING_METHOD_NAME_DUPLICATE";

    // System / Common
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String DATABASE_CONSTRAINT_VIOLATION = "DATABASE_CONSTRAINT_VIOLATION";
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    private ErrorCodes() {
    }
}
