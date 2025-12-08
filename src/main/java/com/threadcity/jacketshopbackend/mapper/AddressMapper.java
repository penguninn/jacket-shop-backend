package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.AddressResponse;
import com.threadcity.jacketshopbackend.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        WardMapper.class,
        DistrictMapper.class,
        ProvinceMapper.class
})
public interface AddressMapper {

    AddressResponse toDto(Address address);

}
