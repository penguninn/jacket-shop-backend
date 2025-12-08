package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.entity.Province;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    ProvinceResponse toDto(Province province);
}
