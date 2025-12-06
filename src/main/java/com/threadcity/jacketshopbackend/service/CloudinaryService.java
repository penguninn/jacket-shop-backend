package com.threadcity.jacketshopbackend.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.threadcity.jacketshopbackend.dto.response.ImageUploadResponse;
import com.threadcity.jacketshopbackend.exception.CloudinaryServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public ImageUploadResponse uploadImages(MultipartFile file) {
        validateFile(file);
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "blog-assets"));
            return ImageUploadResponse.builder()
                    .url(uploadResult.get("secure_url").toString())
                    .build();
        } catch (IOException e) {
            throw new CloudinaryServiceException("Failed to upload asset to Cloudinary: " + e.getMessage());
        } catch (Exception e) {
            throw new CloudinaryServiceException("Unexpected error during asset upload: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CloudinaryServiceException("File cannot be empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new CloudinaryServiceException("Only image files are allowed");
        }
    }

}
