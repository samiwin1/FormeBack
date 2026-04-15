package tn.esprit.partnerbilling.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.partnerbilling.entities.PartnerInvoice;
import tn.esprit.partnerbilling.services.PartnerInvoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/partner-billing")
@CrossOrigin(origins = "http://localhost:4200")
public class PartnerInvoiceController {

    private final PartnerInvoiceService invoiceService;

    public PartnerInvoiceController(PartnerInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoices")
    public List<PartnerInvoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/invoices/partner/{partnerId}")
    public List<PartnerInvoice> getInvoicesByPartner(@PathVariable Long partnerId) {
        return invoiceService.getInvoicesByPartner(partnerId);
    }

    @GetMapping("/invoices/{id}")
    public PartnerInvoice getInvoice(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping("/invoices/generate")
    public PartnerInvoice generateInvoice(@RequestBody PartnerInvoice invoice) {
        return invoiceService.generateInvoice(invoice);
    }

    @PutMapping("/invoices/{id}/pay")
    public PartnerInvoice markAsPaid(@PathVariable Long id) {
        return invoiceService.markAsPaid(id);
    }

    @PutMapping("/invoices/{id}/status")
    public PartnerInvoice updateStatus(@PathVariable Long id, @RequestParam String status) {
        return invoiceService.updateStatus(id, status);
    }
}