package tn.esprit.partnercontract.services;

import org.springframework.stereotype.Service;
import tn.esprit.partnercontract.entities.PartnerContract;
import tn.esprit.partnercontract.repositories.PartnerContractRepository;

import java.util.List;

@Service
public class PartnerContractService {

    private final PartnerContractRepository contractRepository;

    public PartnerContractService(PartnerContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public List<PartnerContract> getAllContracts() {
        return contractRepository.findAll();
    }

    public List<PartnerContract> getContractsByPartner(Long partnerId) {
        return contractRepository.findByPartnerId(partnerId);
    }

    public PartnerContract getContractById(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    public PartnerContract createContract(PartnerContract contract) {
        return contractRepository.save(contract);
    }

    public PartnerContract updateContractStatus(Long id, String status) {
        PartnerContract contract = getContractById(id);
        contract.setStatus(status);
        return contractRepository.save(contract);
    }

    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }
}