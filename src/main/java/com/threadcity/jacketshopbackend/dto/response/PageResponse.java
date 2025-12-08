package com.threadcity.jacketshopbackend.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {

    private int page;

    private int size;

    private int totalPages;

    private long totalElements;

    private T contents;
}
