package com.threadcity.jacketshopbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.threadcity.jacketshopbackend.dto.response.ImageUploadResponse;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ExternalServiceException;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public ImageUploadResponse uploadImages(MultipartFile file) {
        log.info("CloudinaryService::uploadImages execution started");
        validateFile(file);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "blog-assets"));
            log.info("CloudinaryService::uploadImages execution ended");
            return ImageUploadResponse.builder()
                    .url(uploadResult.get("secure_url").toString())
                    .build();
        } catch (IOException e) {
            log.error("Cloudinary upload failed (IOException)", e);
            throw new ExternalServiceException(ErrorCodes.CLOUDINARY_UPLOAD_FAILED,
                    "Failed to upload asset to Cloudinary", "CloudinaryService", e);
        } catch (Exception e) {
            log.error("Cloudinary upload failed (Exception)", e);
            throw new ExternalServiceException(ErrorCodes.CLOUDINARY_UPLOAD_FAILED,
                    "Unexpected error during asset upload to Cloudinary", "CloudinaryService", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.CLOUDINARY_INVALID_FILE, "File cannot be empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InvalidRequestException(ErrorCodes.CLOUDINARY_INVALID_FILE, "Only image files are allowed");
        }
    }
}
