package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.location.response.DistrictResponse;
import com.threadcity.jacketshopbackend.entity.District;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DistrictMapper {

    @Mapping(target = "provinceId", source = "province.id")
    DistrictResponse toResponse(District entity);

    List<DistrictResponse> toResponseList(List<District> entities);
}
