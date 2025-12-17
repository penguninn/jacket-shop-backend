package com.threadcity.jacketshopbackend.configuration;

import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.service.AddressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSyncConfig {

    private final AddressSyncService addressSyncService;
    private final ProvinceRepository provinceRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initDataOnStartup() {
        if (provinceRepository.count() == 0) {
            log.info("Database empty. Starting initial address sync...");
            addressSyncService.syncAllAddressData();
        }
    }
}
