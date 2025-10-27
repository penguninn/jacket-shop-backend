package com.threadcity.jacketshopbackend.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
