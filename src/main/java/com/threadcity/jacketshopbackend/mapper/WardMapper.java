package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.location.response.WardResponse;
import com.threadcity.jacketshopbackend.entity.Ward;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WardMapper {

    @Mapping(target = "districtId", source = "district.id")
    WardResponse toResponse(Ward entity);

    List<WardResponse> toResponseList(List<Ward> entities);
}
