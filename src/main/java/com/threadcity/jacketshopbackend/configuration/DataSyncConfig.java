package com.threadcity.jacketshopbackend.configuration;

import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.service.AddressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// @Configuration
// @EnableScheduling
// @RequiredArgsConstructor
// @Slf4j
// public class DataSyncConfig {
//
//     private final AddressSyncService addressSyncService;
//     private final ProvinceRepository provinceRepository;
//
//     @EventListener(ApplicationReadyEvent.class)
//     public void initDataOnStartup() {
//         if (provinceRepository.count() == 0) {
//             log.info("Database empty. Starting initial address sync...");
//             addressSyncService.syncAllAddressData();
//         }
//     }
//
//     @Scheduled(cron = "0 0 2 * * SUN")
//     public void scheduledSync() {
//         log.info("Starting scheduled address sync...");
//         addressSyncService.syncAllAddressData();
//     }
// }
