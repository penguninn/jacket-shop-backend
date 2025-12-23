package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.goship.location.GoshipCity;
import com.threadcity.jacketshopbackend.dto.goship.location.GoshipDistrict;
import com.threadcity.jacketshopbackend.dto.goship.GoshipResponse;
import com.threadcity.jacketshopbackend.dto.goship.location.GoshipWard;
import com.threadcity.jacketshopbackend.entity.District;
import com.threadcity.jacketshopbackend.entity.Province;
import com.threadcity.jacketshopbackend.entity.Ward;
import com.threadcity.jacketshopbackend.repository.DistrictRepository;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    @Value("${goship.url}")
    private String goshipUrl;

    @Value("${goship.token}")
    private String goshipToken;

    private RestClient getRestClient() {
        return RestClient.builder()
                .baseUrl(goshipUrl)
                .defaultHeader("Authorization", "Bearer " + goshipToken)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public void syncAllAddressData() {
        log.info("START SYNCING ADDRESS DATA FROM GOSHIP...");

        try {
            syncProvinces();

            List<Province> provinces = provinceRepository.findAll();
            for (Province p : provinces) {
                if (p.getGoshipId() != null) {
                    syncDistricts(p);
                }
            }

            List<District> districts = districtRepository.findAll();
            for (District d : districts) {
                if (d.getGoshipId() != null) {
                    syncWards(d);
                }
            }

            log.info("SYNC ADDRESS DATA COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            log.error("Error syncing address data: {}", e.getMessage(), e);
        }
    }

    private void syncProvinces() {
        log.info("Fetching cities...");
        try {
            GoshipResponse<GoshipCity> response = getRestClient().get()
                    .uri("/cities")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.getData() != null) {
                for (GoshipCity c : response.getData()) {
                    String goshipId = c.getId();
                    Optional<Province> existing = provinceRepository.findByGoshipId(goshipId);

                    Province province;
                    if (existing.isPresent()) {
                        province = existing.get();
                        province.setName(c.getName());
                    } else {
                        province = Province.builder()
                                .goshipId(goshipId)
                                .name(c.getName())
                                .build();
                    }
                    provinceRepository.save(province);
                }
            }
        } catch (Exception e) {
            log.error("Failed to sync cities: {}", e.getMessage());
        }
    }

    private void syncDistricts(Province province) {
        try {
            String cityCode = province.getGoshipId().toString();

            GoshipResponse<GoshipDistrict> response = getRestClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/cities/{code}/districts").build(cityCode))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.getData() != null) {
                for (GoshipDistrict d : response.getData()) {
                    String goshipId = d.getId();
                    Optional<District> existing = districtRepository.findByGoshipId(goshipId);

                    District district;
                    if (existing.isPresent()) {
                        district = existing.get();
                        district.setName(d.getName());
                        district.setProvince(province);
                    } else {
                        district = District.builder()
                                .goshipId(goshipId)
                                .name(d.getName())
                                .province(province)
                                .build();

                    }
                    districtRepository.save(district);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch districts for city {}: {}", province.getName(), e.getMessage());
        }
    }

    private void syncWards(District district) {
        try {
            String districtCode = district.getGoshipId().toString();

            GoshipResponse<GoshipWard> response = getRestClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/districts/{code}/wards").build(districtCode))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response != null && response.getData() != null) {
                for (GoshipWard w : response.getData()) {
                    String goshipId = w.getId();
                    Optional<Ward> existing = wardRepository.findByGoshipId(goshipId);

                    Ward ward;
                    if (existing.isPresent()) {
                        ward = existing.get();
                        ward.setName(w.getName());
                        ward.setDistrict(district);
                    } else {
                        ward = Ward.builder()
                                .goshipId(goshipId)
                                .name(w.getName())
                                .district(district)
                                .build();

                    }
                    wardRepository.save(ward);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch wards for district {}: {}", district.getName(), e.getMessage());
        }
    }
}
