package com.example.controller;

import com.example.entity.AccessCode;
import com.example.service.AccessCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-codes")
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    public AccessCodeController(AccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    // Get all access codes
    @GetMapping
    public List<AccessCode> getAllAccessCodes() {
        return accessCodeService.getAllAccessCodes();
    }

    // Get access code by id
    @GetMapping("/{id}")
    public ResponseEntity<AccessCode> getAccessCodeById(@PathVariable Long id) {
        return accessCodeService.getAccessCodeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create access code
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccessCode createAccessCode(@RequestBody AccessCode accessCode) {
        return accessCodeService.createAccessCode(accessCode);
    }

    // Update access code
    @PutMapping("/{id}")
    public ResponseEntity<AccessCode> updateAccessCode(@PathVariable Long id, @RequestBody AccessCode accessCode) {
        return ResponseEntity.ok(accessCodeService.updateAccessCode(id, accessCode));
    }

    // Delete access code
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccessCode(@PathVariable Long id) {
        accessCodeService.deleteAccessCode(id);
        return ResponseEntity.noContent().build();
    }

    // 🔥 BUSINESS FEATURE : use access code
    @PostMapping("/use/{code}")
    public ResponseEntity<AccessCode> useAccessCode(@PathVariable String code) {
        AccessCode usedCode = accessCodeService.useCode(code);
        return ResponseEntity.ok(usedCode);
    }
}