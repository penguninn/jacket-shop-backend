package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    @Mapping(target = "goShipId", source = "goshipId")
    ProvinceResponse toDto(Province province);
}
