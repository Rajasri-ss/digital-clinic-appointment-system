package com.digitalclinic.appointmentsystem.controller;

import com.digitalclinic.appointmentsystem.dto.HospitalDTO;
import com.digitalclinic.appointmentsystem.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HospitalDTO>> searchHospitals(
            @RequestParam String query) {
        return ResponseEntity.ok(hospitalService.searchHospitals(query));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<HospitalDTO>> getHospitalsByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCity(city));
    }
}