package com.example.controller;

import com.example.entity.Partner;
import com.example.service.PartnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    // Get all partners
    @GetMapping
    public List<Partner> getAllPartners() {
        return partnerService.getAllPartners();
    }

    // Get partner by id
    @GetMapping("/{id}")
    public ResponseEntity<Partner> getPartnerById(@PathVariable Long id) {
        return partnerService.getPartnerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create partner
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Partner createPartner(@RequestBody Partner partner) {
        return partnerService.createPartner(partner);
    }

    // Update partner
    @PutMapping("/{id}")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner partner) {
        return ResponseEntity.ok(partnerService.updatePartner(id, partner));
    }

    // Delete partner
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }

    // 🔥 Partner statistics
    @GetMapping("/{id}/stats")
    public Map<String, Object> getPartnerStats(@PathVariable Long id){
        return partnerService.getPartnerStats(id);
    }

    // Test endpoint
    @GetMapping("/test")
    public String test() {
        return "OK";
    }
}