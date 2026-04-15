package tn.esprit.partnercontract.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.partnercontract.entities.PartnerContract;
import tn.esprit.partnercontract.services.PartnerContractService;

import java.util.List;

@RestController
@RequestMapping("/api/partner-contracts")
@CrossOrigin(origins = "http://localhost:4200") // Allowing frontend proxy dev server
public class PartnerContractController {

    private final PartnerContractService contractService;

    public PartnerContractController(PartnerContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<PartnerContract> getAllContracts() {
        return contractService.getAllContracts();
    }

    @GetMapping("/partner/{partnerId}")
    public List<PartnerContract> getContractsByPartner(@PathVariable Long partnerId) {
        return contractService.getContractsByPartner(partnerId);
    }

    @GetMapping("/{id}")
    public PartnerContract getContract(@PathVariable Long id) {
        return contractService.getContractById(id);
    }

    @PostMapping
    public PartnerContract createContract(@RequestBody PartnerContract contract) {
        return contractService.createContract(contract);
    }

    @PutMapping("/{id}/status")
    public PartnerContract updateStatus(@PathVariable Long id, @RequestParam String status) {
        return contractService.updateContractStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
    }
}