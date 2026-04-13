package com.example.service;

import com.example.entity.Pack;
import com.example.repository.PackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackService {

    private final PackRepository packRepository;

    public PackService(PackRepository packRepository) {
        this.packRepository = packRepository;
    }

    public List<Pack> getAllPacks() {
        return packRepository.findAll();
    }

    public Optional<Pack> getPackById(Long id) {
        return packRepository.findById(id);
    }

    public Pack createPack(Pack pack) {
        return packRepository.save(pack);
    }

    public Pack updatePack(Long id, Pack updatedPack) {
        return packRepository.findById(id)
                .map(pack -> {
                    pack.setName(updatedPack.getName());
                    pack.setDescription(updatedPack.getDescription());
                    pack.setValidityMonths(updatedPack.getValidityMonths());
                    pack.setActive(updatedPack.isActive());
                    return packRepository.save(pack);
                })
                .orElseGet(() -> {
                    updatedPack.setId(id);
                    return packRepository.save(updatedPack);
                });
    }

    public void deletePack(Long id) {
        if (packRepository.existsById(id)) {
            packRepository.deleteById(id);
        }
    }
}
