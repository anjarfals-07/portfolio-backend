package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.PlatformInfoDTO;
import com.anjar.portfolio.dto.PlatformSettingDTO;
import com.anjar.portfolio.service.PlatformSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PlatformSettingService settingService;

    @GetMapping("/platform-info")
    public ResponseEntity<PlatformInfoDTO> getPlatformInfo() {
        PlatformSettingDTO s = settingService.getSettings();

        PlatformInfoDTO dto = PlatformInfoDTO.builder()
                .platformName(s.getPlatformName())
                .tagline(s.getTagline())
                .logoIcon(s.getLogoIcon())
                .primaryColor(s.getPrimaryColor())
                .accentColor(s.getAccentColor())
                .defaultPortfolioUsername(s.getDefaultPortfolioUsername())
                .primaryDomain(s.getPrimaryDomain())
                .registrationPaymentEnabled(s.getRegistrationPaymentEnabled())
                .registrationFeeIdr(s.getRegistrationFeeIdr())
                .paymentExpiryMinutes(s.getPaymentExpiryMinutes())
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(dto);
    }
}