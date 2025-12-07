package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);
}
