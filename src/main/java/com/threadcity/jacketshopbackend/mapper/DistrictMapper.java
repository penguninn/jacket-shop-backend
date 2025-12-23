package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.DistrictResponse;
import com.threadcity.jacketshopbackend.entity.District;
import com.threadcity.jacketshopbackend.entity.Province;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DistrictMapper {

    @Mapping(target = "provinceId", source = "province", qualifiedByName = "provinceToProvinceId")
    @Mapping(target = "goShipId", source = "goshipId")
    DistrictResponse toDto(District district);
    
    @Named("provinceToProvinceId")
    public static Long provinceToProvinceId(Province province) {
        return province.getId();
    }
}
