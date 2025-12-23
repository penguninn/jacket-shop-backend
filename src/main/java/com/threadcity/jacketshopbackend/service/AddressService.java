package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.AddressRequest;
import com.threadcity.jacketshopbackend.dto.response.*;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.AuthorizationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.AddressMapper;
import com.threadcity.jacketshopbackend.mapper.DistrictMapper;
import com.threadcity.jacketshopbackend.mapper.ProvinceMapper;
import com.threadcity.jacketshopbackend.mapper.WardMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final ProvinceMapper provinceMapper;
    private final DistrictMapper districtMapper;
    private final WardMapper wardMapper;

    // --- Administrative Division Methods ---

    public List<ProvinceResponse> getAllProvinces() {
        log.info("AddressService::getAllProvinces - Execution started");
        List<ProvinceResponse> provinceResponses = provinceRepository.findAll().stream()
                .map(provinceMapper::toDto)
                .toList();
        log.info("AddressService::getAllProvinces - Execution ended");
        return provinceResponses;
    }

    public List<DistrictResponse> getAllDistricts(Long provinceId) {
        log.info("AddressService::getAllDistricts - Execution started");
        List<DistrictResponse> districtResponses = districtRepository.findAllByProvinceId(provinceId)
                .stream()
                .map(districtMapper::toDto)
                .toList();
        log.info("AddressService::getAllDistricts - Execution ended");
        return districtResponses;
    }

    public List<WardResponse> getAllWards(Long districtId) {
        log.info("AddressService::getAllWards - Execution started");
        List<WardResponse> wardResponses = wardRepository.findAllByDistrictId(districtId)
                .stream()
                .map(wardMapper::toDto)
                .toList();
        log.info("AddressService::getAllWards - Execution ended");
        return wardResponses;
    }

    // --- User Address Methods ---

    public List<AddressResponse> getAllAddressByUserId() {
        log.info("AddressService::getAllAddressByUserId - Execution started");
        Long userId = getUserId();
        List<AddressResponse> addressReponses = addressRepository.findAllByUserId(userId)
                .stream()
                .map(addressMapper::toDto)
                .toList();
        log.info("AddressService::getAllAddressByUserId - Execution completed");
        return addressReponses;
    }

    public AddressResponse getDefaultAddress() {
        log.info("AddressService::getDefaultAddress - Execution started");
        Long userId = getUserId();
        AddressResponse addressReponse = addressRepository
                .findByUserIdAndIsDefaultTrue(userId)
                .map(addressMapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Default address not found"));
        log.info("AddressService::getDefaultAddress - Execution completed");
        return addressReponse;
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        log.info("AddressService::createAddress - Execution started");
        Long userId = getUserId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
        
        Province province = provinceRepository.findById(request.getProvinceId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Province not found"));
        
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "District not found"));
        
        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Ward not found"));

        validateAddressHierarchy(province, district, ward);

        Address address = new Address();
        address.setUser(user);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        address.setAddressLine(request.getAddressLine());
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());

        long count = addressRepository.countByUserId(userId);
        if (count == 0) {
            address.setIsDefault(true);
        } else {
            if (Boolean.TRUE.equals(request.getIsDefault())) {
                addressRepository.clearDefaultAddressForUser(userId);
                address.setIsDefault(true);
            } else {
                address.setIsDefault(false);
            }
        }
        
        Address saved = addressRepository.save(address);
        log.info("AddressService::createAddress - Execution completed");
        return addressMapper.toDto(saved);
    }

    @Transactional
    public AddressResponse updateAddress(AddressRequest request, Long addressId) {
        log.info("AddressService::updateAddress - Execution started. [id: {}]", addressId);
        Long userId = getUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "Not owner of address");
        }

        Province newProvince = request.getProvinceId() != null
                ? provinceRepository.findById(request.getProvinceId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Province not found"))
                : address.getProvince();

        District newDistrict = request.getDistrictId() != null
                ? districtRepository.findById(request.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "District not found"))
                : address.getDistrict();

        Ward newWard = request.getWardId() != null
                ? wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Ward not found"))
                : address.getWard();

        validateAddressHierarchy(newProvince, newDistrict, newWard);

        address.setProvince(newProvince);
        address.setDistrict(newDistrict);
        address.setWard(newWard);

        if (request.getAddressLine() != null) address.setAddressLine(request.getAddressLine());
        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getRecipientPhone() != null) address.setRecipientPhone(request.getRecipientPhone());

        if (Boolean.FALSE.equals(request.getIsDefault()) && Boolean.TRUE.equals(address.getIsDefault())) {
            long count = addressRepository.countByUserId(userId);
            if (count == 1) {
                throw new InvalidRequestException(ErrorCodes.ADDRESS_CANNOT_UNSET_DEFAULT,
                        "Cannot unset default address when there is only address");
            }
            throw new InvalidRequestException(ErrorCodes.ADDRESS_SET_ANOTHER_DEFAULT,
                    "Set another default address first");
        }
        
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddressForUser(userId);
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        log.info("AddressService::updateAddress - Execution completed. [id: {}]", addressId);
        return addressMapper.toDto(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        log.info("AddressService::setDefaultAddress - Execution started. [id: {}]", addressId);
        Long userId = getUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "Not owner of address");
        }
        addressRepository.clearDefaultAddressForUser(userId);
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);
        log.info("AddressService::setDefaultAddress - Execution completed. [id: {}]", addressId);
        return addressMapper.toDto(saved);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        log.info("AddressService::deleteAddress - Execution started. [id: {}]", addressId);
        Long userId = getUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "Not owner of address");
        }
        if (address.getIsDefault()) {
            long count = addressRepository.countByUserId(userId);
            if (count > 1) {
                throw new InvalidRequestException(ErrorCodes.ADDRESS_DELETE_DEFAULT,
                        "Let set other address is default before delete");
            }
            throw new InvalidRequestException(ErrorCodes.ADDRESS_DELETE_LAST_DEFAULT,
                    "Cannot delete only address default");
        }
        addressRepository.delete(address);
        log.info("AddressService::deleteAddress - Execution completed. [id: {}]", addressId);
    }

    private void validateAddressHierarchy(Province province, District district, Ward ward) {
        if (!district.getProvince().getId().equals(province.getId())) {
            throw new InvalidRequestException(ErrorCodes.ADDRESS_INVALID_HIERARCHY, 
                String.format("District '%s' does not belong to Province '%s'", district.getName(), province.getName()));
        }
        if (!ward.getDistrict().getId().equals(district.getId())) {
             throw new InvalidRequestException(ErrorCodes.ADDRESS_INVALID_HIERARCHY, 
                String.format("Ward '%s' does not belong to District '%s'", ward.getName(), district.getName()));
        }
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}