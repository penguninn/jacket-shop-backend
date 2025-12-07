package com.threadcity.jacketshopbackend.service;

import java.util.List;

import com.threadcity.jacketshopbackend.exception.AuthorizationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.request.AddressRequest;
import com.threadcity.jacketshopbackend.dto.response.AddressResponse;
import com.threadcity.jacketshopbackend.entity.Address;
import com.threadcity.jacketshopbackend.entity.District;
import com.threadcity.jacketshopbackend.entity.Province;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.entity.Ward;
import com.threadcity.jacketshopbackend.mapper.AddressMapper;
import com.threadcity.jacketshopbackend.repository.AddressRepository;
import com.threadcity.jacketshopbackend.repository.DistrictRepository;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.repository.WardRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {

    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public List<AddressResponse> getAllAddressByUserId() {
        log.info("UserAddressService::getAllAddressByUserId - Execution started");
        Long userId = getUserId();
        List<AddressResponse> addressReponses = addressRepository.findAllByUserId(userId)
                .stream()
                .map(addressMapper::toDto)
                .toList();
        log.info("UserAddressService::getAllAddressByUserId - Execution ended");
        return addressReponses;
    }

    public AddressResponse getDefaultAddress() {
        log.info("UserAddressService::getDefaultAddress - Execution started");
        Long userId = getUserId();
        AddressResponse addressReponse = addressRepository
                .findByUserIdAndIsDefaultTrue(userId)
                .map(addressMapper::toDto)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Default address not found"));
        log.info("UserAddressService::getDefaultAddress - Execution ended");
        return addressReponse;
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        log.info("UserAddressService::createAddress - Execution started");
        Long userId = getUserId();
        Address address = new Address();
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
            address.setUser(user);
        }
        if (request.getProvinceId() != null) {
            Province province = provinceRepository.findById(request.getProvinceId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Province not found"));
            address.setProvince(province);
        }
        if (request.getDistrictId() != null) {
            District district = districtRepository.findById(request.getDistrictId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "District not found"));
            address.setDistrict(district);
        }
        if (request.getWardId() != null) {
            Ward ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Ward not found"));
            address.setWard(ward);
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddressForUser(userId);
            address.setIsDefault(true);
        }
        address.setAddressLine(request.getAddressLine());
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        Address saved = addressRepository.save(address);
        log.info("UserAddressService::createAddress - Execution ended");
        return addressMapper.toDto(saved);
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        log.info("UserAddressService::updateAddress - Execution started");
        Long userId = getUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "Not owner of address");
        }
        if (request.getProvinceId() != null) {
            Province province = provinceRepository.findById(request.getProvinceId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Province not found"));
            address.setProvince(province);
        }
        if (request.getDistrictId() != null) {
            District district = districtRepository.findById(request.getDistrictId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "District not found"));
            address.setDistrict(district);
        }
        if (request.getWardId() != null) {
            Ward ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Ward not found"));
            address.setWard(ward);
        }
        if (Boolean.FALSE.equals(request.getIsDefault()) && Boolean.TRUE.equals(address.getIsDefault())) {
            long count = addressRepository.countByUserId(userId);

            if (count == 1) {
                throw new InvalidRequestException(ErrorCodes.ADDRESS_CANNOT_UNSET_DEFAULT,
                        "Cannot unset default address when there is only address");
            }

            throw new InvalidRequestException(ErrorCodes.ADDRESS_SET_ANOTHER_DEFAULT,
                    "Set another default address first");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddressForUser(userId);
            address.setIsDefault(true);
        }
        address.setAddressLine(request.getAddressLine());
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        Address saved = addressRepository.save(address);
        log.info("UserAddressService::createAddress - Execution ended");
        return addressMapper.toDto(saved);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        log.info("UserAddressService::setDefaultAddress - Execution started");
        Long userId = getUserId();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "Not owner of address");
        }
        addressRepository.clearDefaultAddressForUser(userId);
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);
        log.info("UserAddressService::setDefaultAddress - Execution ended");
        return addressMapper.toDto(saved);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        log.info("UserAddressService::deleteAddress - Execution started");
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
        log.info("UserAddressService::deleteAddress - Execution ended");
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
