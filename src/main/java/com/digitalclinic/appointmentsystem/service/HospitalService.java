package com.digitalclinic.appointmentsystem.service;

import com.digitalclinic.appointmentsystem.dto.HospitalDTO;
import com.digitalclinic.appointmentsystem.model.Hospital;
import com.digitalclinic.appointmentsystem.repository.HospitalRepository;
import com.digitalclinic.appointmentsystem.repository.DoctorRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<HospitalDTO> getAllHospitals() {
        logger.info("Fetching all active hospitals");
        return hospitalRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public HospitalDTO getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        return convertToDTO(hospital);
    }

    public List<HospitalDTO> searchHospitals(String query) {
        logger.info("Searching hospitals with query: {}", query);
        return hospitalRepository.searchHospitals(query)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<HospitalDTO> getHospitalsByCity(String city) {
        return hospitalRepository.findByCityIgnoreCase(city)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HospitalDTO convertToDTO(Hospital hospital) {
        HospitalDTO dto = modelMapper.map(hospital, HospitalDTO.class);
        dto.setId(hospital.getId());
        return dto;
    }
}