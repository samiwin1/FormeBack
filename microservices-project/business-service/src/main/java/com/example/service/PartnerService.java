package com.example.service;

import com.example.entity.Partner;
import com.example.repository.PartnerRepository;
import com.example.repository.DealRepository;
import com.example.repository.AccessCodeRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final DealRepository dealRepository;
    private final AccessCodeRepository accessCodeRepository;

    public PartnerService(PartnerRepository partnerRepository,
                          DealRepository dealRepository,
                          AccessCodeRepository accessCodeRepository) {
        this.partnerRepository = partnerRepository;
        this.dealRepository = dealRepository;
        this.accessCodeRepository = accessCodeRepository;
    }

    // Get all partners
    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    // Get partner by id
    public Optional<Partner> getPartnerById(Long id) {
        return partnerRepository.findById(id);
    }

    // Create partner
    public Partner createPartner(Partner partner) {
        return partnerRepository.save(partner);
    }

    // Update partner
    public Partner updatePartner(Long id, Partner updatedPartner) {
        return partnerRepository.findById(id)
                .map(partner -> {
                    partner.setName(updatedPartner.getName());
                    partner.setContactEmail(updatedPartner.getContactEmail());
                    partner.setContactPhone(updatedPartner.getContactPhone());
                    return partnerRepository.save(partner);
                })
                .orElseGet(() -> {
                    updatedPartner.setId(id);
                    return partnerRepository.save(updatedPartner);
                });
    }

    // Delete partner
    public void deletePartner(Long id) {
        if (partnerRepository.existsById(id)) {
            partnerRepository.deleteById(id);
        }
    }

    // 🔥 Partner statistics
    public Map<String, Object> getPartnerStats(Long partnerId){

        Long deals = dealRepository.countDealsByPartner(partnerId);
        Long usedCodes = accessCodeRepository.countUsedCodes(partnerId);

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalDeals", deals);
        stats.put("usedCodes", usedCodes);

        return stats;
    }
}