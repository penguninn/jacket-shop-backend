package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.AddressRequest;
import com.threadcity.jacketshopbackend.dto.response.AddressResponse;
import com.threadcity.jacketshopbackend.entity.Address;
import com.threadcity.jacketshopbackend.entity.District;
import com.threadcity.jacketshopbackend.entity.Province;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.entity.Ward;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.AddressMapper;
import com.threadcity.jacketshopbackend.repository.AddressRepository;
import com.threadcity.jacketshopbackend.repository.DistrictRepository;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.repository.WardRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProvinceRepository provinceRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private WardRepository wardRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private AddressMapper addressMapper;

    @InjectMocks
    private UserAddressService userAddressService;

    @BeforeEach
    void setupSecurityContext() {
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllAddressByUserId_shouldReturnList() {
        Address address = new Address();
        AddressResponse response = new AddressResponse();

        when(addressRepository.findAllByUserId(1L))
                .thenReturn(List.of(address));

        when(addressMapper.toDto(address))
                .thenReturn(response);

        List<AddressResponse> result =
                userAddressService.getAllAddressByUserId();

        assertEquals(1, result.size());
        verify(addressRepository).findAllByUserId(1L);
        verify(addressMapper).toDto(address);
    }
    @Test
    void createAddress_firstAddress_shouldSetDefaultTrue() {
        // GIVEN
        AddressRequest request = mock(AddressRequest.class);

        when(request.getProvinceId()).thenReturn(1L);
        when(request.getDistrictId()).thenReturn(2L);
        when(request.getWardId()).thenReturn(3L);
        when(request.getAddressLine()).thenReturn("123 Test Street");
        when(request.getRecipientName()).thenReturn("Huy");
        when(request.getRecipientPhone()).thenReturn("0123456789");

        User user = new User();
        user.setId(1L);

        Province province = new Province();
        province.setId(1L);

        District district = new District();
        district.setId(2L);
        district.setProvince(province);

        Ward ward = new Ward();
        ward.setId(3L);
        ward.setDistrict(district);

        Address savedAddress = new Address();
        savedAddress.setIsDefault(true);

        AddressResponse response = new AddressResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(2L)).thenReturn(Optional.of(district));
        when(wardRepository.findById(3L)).thenReturn(Optional.of(ward));
        when(addressRepository.countByUserId(1L)).thenReturn(0L);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
        when(addressMapper.toDto(savedAddress)).thenReturn(response);

        // WHEN
        AddressResponse result = userAddressService.createAddress(request);

        // THEN
        assertNotNull(result);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_notFirstAddress_shouldRespectRequestDefault() {
        // GIVEN
        AddressRequest request = mock(AddressRequest.class);

        when(request.getProvinceId()).thenReturn(1L);
        when(request.getDistrictId()).thenReturn(2L);
        when(request.getWardId()).thenReturn(3L);
        when(request.getAddressLine()).thenReturn("456 Another Street");
        when(request.getRecipientName()).thenReturn("Huy");
        when(request.getRecipientPhone()).thenReturn("0123456789");
        when(request.getIsDefault()).thenReturn(false);

        User user = new User();
        user.setId(1L);

        Province province = new Province();
        province.setId(1L);

        District district = new District();
        district.setId(2L);
        district.setProvince(province);

        Ward ward = new Ward();
        ward.setId(3L);
        ward.setDistrict(district);

        Address savedAddress = new Address();
        savedAddress.setIsDefault(false);

        AddressResponse response = new AddressResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(2L)).thenReturn(Optional.of(district));
        when(wardRepository.findById(3L)).thenReturn(Optional.of(ward));
        when(addressRepository.countByUserId(1L)).thenReturn(2L);
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
        when(addressMapper.toDto(savedAddress)).thenReturn(response);

        // WHEN
        AddressResponse result = userAddressService.createAddress(request);

        // THEN
        assertNotNull(result);
        verify(addressRepository).save(argThat(address -> !address.getIsDefault()));
    }
    @Test
    void createAddress_userNotFound_shouldThrowException() {
        AddressRequest request = AddressRequest.builder()
                .provinceId(1L)
                .districtId(2L)
                .wardId(3L)
                .addressLine("123 ABC")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userAddressService.createAddress(request)
        );

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(
                provinceRepository,
                districtRepository,
                wardRepository,
                addressRepository
        );
    }

    @Test
    void createAddress_setNewDefault_shouldUnsetOldDefaults() {

        AddressRequest request = AddressRequest.builder()
                .addressLine("New Default Address")
                .wardId(3L)
                .districtId(2L)
                .provinceId(1L)
                .recipientPhone("0123456789")
                .isDefault(true)
                .build();

        User user = new User();
        user.setId(1L);

        Province province = new Province();
        province.setId(1L);

        District district = new District();
        district.setId(2L);
        district.setProvince(province);

        Ward ward = new Ward();
        ward.setId(3L);
        ward.setDistrict(district);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(province));
        when(districtRepository.findById(2L)).thenReturn(Optional.of(district));
        when(wardRepository.findById(3L)).thenReturn(Optional.of(ward));

        when(addressRepository.countByUserId(1L)).thenReturn(2L);

        Address savedAddress = new Address();
        savedAddress.setIsDefault(true);

        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
        when(addressMapper.toDto(savedAddress)).thenReturn(new AddressResponse());

        // WHEN
        userAddressService.createAddress(request);

        // THEN
        verify(addressRepository).save(any(Address.class));
    }



}
