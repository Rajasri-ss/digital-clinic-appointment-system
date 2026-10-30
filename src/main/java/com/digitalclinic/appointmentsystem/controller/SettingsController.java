package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.model.SystemSetting;
import com.digitalclinic.appointmentsystem.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    private final SystemSettingRepository systemSettingRepository;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAllSettings() {
        return ResponseEntity.ok(systemSettingRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<SystemSetting> updateSetting(@RequestBody SystemSetting setting) {
        SystemSetting existing = systemSettingRepository.findBySettingKey(setting.getSettingKey()).orElse(null);
        if (existing != null) {
            existing.setSettingValue(setting.getSettingValue());
            existing.setDescription(setting.getDescription());
            return ResponseEntity.ok(systemSettingRepository.save(existing));
        }
        return ResponseEntity.ok(systemSettingRepository.save(setting));
    }
}
