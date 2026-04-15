package tn.esprit.partnerbilling.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.partnerbilling.entities.PartnerInvoice;

import java.util.List;

@Repository
public interface PartnerInvoiceRepository extends JpaRepository<PartnerInvoice, Long> {
    List<PartnerInvoice> findByPartnerId(Long partnerId);
    List<PartnerInvoice> findByStatus(String status);
}