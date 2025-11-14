package com.threadcity.jacketshopbackend.mapper;
import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toDto(Category category);

    Category toEntity(CategoryRequest request);
}
