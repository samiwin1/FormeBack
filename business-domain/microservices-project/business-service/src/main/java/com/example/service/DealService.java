package com.example.service;

import com.example.entity.Deal;
import com.example.repository.DealRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DealService {

    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    // Get all deals
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    // Get deal by id
    public Optional<Deal> getDealById(Long id) {
        return dealRepository.findById(id);
    }

    // Create deal
    public Deal createDeal(Deal deal) {
        return dealRepository.save(deal);
    }

    // Update deal
    public Deal updateDeal(Long id, Deal updatedDeal) {
        return dealRepository.findById(id)
                .map(deal -> {
                    deal.setTitle(updatedDeal.getTitle());
                    deal.setDescription(updatedDeal.getDescription());
                    deal.setPartnerId(updatedDeal.getPartnerId());
                    deal.setStartDate(updatedDeal.getStartDate());
                    deal.setEndDate(updatedDeal.getEndDate());
                    return dealRepository.save(deal);
                })
                .orElseGet(() -> {
                    updatedDeal.setId(id);
                    return dealRepository.save(updatedDeal);
                });
    }

    // Delete deal
    public void deleteDeal(Long id) {
        if (dealRepository.existsById(id)) {
            dealRepository.deleteById(id);
        }
    }
}