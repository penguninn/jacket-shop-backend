package com.threadcity.jacketshopbackend.mapper;

import org.mapstruct.Mapper;

import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.entity.Province;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    ProvinceResponse toDto(Province province);
}
