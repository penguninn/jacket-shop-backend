package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageUploadResponse implements Serializable {

    private String url;
    private Integer width;
    private Integer height;
    private String format;
    private Long bytes;
}
