package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.location.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.entity.Province;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {

    ProvinceResponse toResponse(Province entity);

    List<ProvinceResponse> toResponseList(List<Province> entities);
}
