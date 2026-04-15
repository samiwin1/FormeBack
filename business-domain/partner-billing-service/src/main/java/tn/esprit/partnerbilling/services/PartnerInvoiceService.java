package tn.esprit.partnerbilling.services;

import org.springframework.stereotype.Service;
import tn.esprit.partnerbilling.entities.PartnerInvoice;
import tn.esprit.partnerbilling.repositories.PartnerInvoiceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartnerInvoiceService {

    private final PartnerInvoiceRepository invoiceRepository;

    public PartnerInvoiceService(PartnerInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<PartnerInvoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<PartnerInvoice> getInvoicesByPartner(Long partnerId) {
        return invoiceRepository.findByPartnerId(partnerId);
    }

    public PartnerInvoice generateInvoice(PartnerInvoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public PartnerInvoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public PartnerInvoice markAsPaid(Long id) {
        PartnerInvoice invoice = getInvoiceById(id);
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public PartnerInvoice updateStatus(Long id, String status) {
        PartnerInvoice invoice = getInvoiceById(id);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }
}