package com.threadcity.jacketshopbackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.threadcity.jacketshopbackend.dto.response.WardResponse;
import com.threadcity.jacketshopbackend.entity.District;
import com.threadcity.jacketshopbackend.entity.Ward;

@Mapper(componentModel = "spring")
public interface WardMapper {

    @Mapping(target = "districtId", source = "district", qualifiedByName = "districtToDistrictId")
    WardResponse toDto(Ward ward);

    @Named("districtToDistrictId")
    public static Long districtToDistrictId(District district) {
        return district.getId();
    }
}
