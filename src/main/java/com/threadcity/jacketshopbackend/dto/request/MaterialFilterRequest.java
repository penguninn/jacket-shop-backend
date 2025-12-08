package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MaterialFilterRequest {

    @Size(max = 255, message = "Search term too long")
    private String search;           // tìm theo name hoặc description

    private List<String> status;     // list status

    @Builder.Default
    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    private int size = 10;

    @Builder.Default
    @Pattern(
            regexp = "^(id|name|createdAt|updatedAt)$",
            message = "Invalid sort field"
    )
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(
            regexp = "^(ASC|DESC)$",
            message = "Sort direction must be ASC or DESC"
    )
    private String sortDir = "DESC";
}
