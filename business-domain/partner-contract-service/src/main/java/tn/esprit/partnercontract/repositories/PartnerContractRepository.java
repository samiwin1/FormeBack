package tn.esprit.partnercontract.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.partnercontract.entities.PartnerContract;

import java.util.List;

@Repository
public interface PartnerContractRepository extends JpaRepository<PartnerContract, Long> {
    List<PartnerContract> findByPartnerId(Long partnerId);
    List<PartnerContract> findByStatus(String status);
}