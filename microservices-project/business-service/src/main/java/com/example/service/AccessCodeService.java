package com.example.service;

import com.example.entity.AccessCode;
import com.example.repository.AccessCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AccessCodeService {

    private final AccessCodeRepository accessCodeRepository;

    public AccessCodeService(AccessCodeRepository accessCodeRepository) {
        this.accessCodeRepository = accessCodeRepository;
    }

    // Get all access codes
    public List<AccessCode> getAllAccessCodes() {
        return accessCodeRepository.findAll();
    }

    // Get access code by id
    public Optional<AccessCode> getAccessCodeById(Long id) {
        return accessCodeRepository.findById(id);
    }

    // Create new access code
    public AccessCode createAccessCode(AccessCode accessCode) {
        return accessCodeRepository.save(accessCode);
    }

    // Update access code
    public AccessCode updateAccessCode(Long id, AccessCode updatedAccessCode) {
        return accessCodeRepository.findById(id)
                .map(code -> {
                    code.setCode(updatedAccessCode.getCode());
                    code.setPartnerId(updatedAccessCode.getPartnerId());
                    code.setDealId(updatedAccessCode.getDealId());
                    code.setExpirationDate(updatedAccessCode.getExpirationDate());
                    code.setUsed(updatedAccessCode.isUsed());
                    return accessCodeRepository.save(code);
                })
                .orElseGet(() -> {
                    updatedAccessCode.setId(id);
                    return accessCodeRepository.save(updatedAccessCode);
                });
    }

    // Delete access code
    public void deleteAccessCode(Long id) {
        if (accessCodeRepository.existsById(id)) {
            accessCodeRepository.deleteById(id);
        }
    }

    // 🔥 BUSINESS LOGIC : Use Access Code
    public AccessCode useCode(String codeValue) {

        AccessCode accessCode = accessCodeRepository.findByCode(codeValue);

        if (accessCode == null) {
            throw new RuntimeException("Access code not found");
        }

        if (accessCode.isUsed()) {
            throw new RuntimeException("Access code already used");
        }

        if (accessCode.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Access code expired");
        }

        accessCode.setUsed(true);

        return accessCodeRepository.save(accessCode);
    }
}