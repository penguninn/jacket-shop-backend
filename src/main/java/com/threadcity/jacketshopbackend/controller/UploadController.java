package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.ImageUploadResponse;
import com.threadcity.jacketshopbackend.service.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/upload")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadImages(@RequestParam("file") MultipartFile file) {
        ImageUploadResponse uploadResponse = cloudinaryService.uploadImages(file);
        return ApiResponse.builder()
                .code(201)
                .data(uploadResponse)
                .message("Upload images successfully")
                .timestamp(Instant.now())
                .build();
    }
}
