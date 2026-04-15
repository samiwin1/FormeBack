package tn.esprit.partnerperformance.dto;

public class PartnerKpi {
    private Double redemptionRate;
    private Long issued;
    private Long redeemed;
    private Double revenue;

    public PartnerKpi() {}

    public PartnerKpi(Double redemptionRate, Long issued, Long redeemed, Double revenue) {
        this.redemptionRate = redemptionRate;
        this.issued = issued;
        this.redeemed = redeemed;
        this.revenue = revenue;
    }

    public Double getRedemptionRate() { return redemptionRate; }
    public void setRedemptionRate(Double redemptionRate) { this.redemptionRate = redemptionRate; }
    public Long getIssued() { return issued; }
    public void setIssued(Long issued) { this.issued = issued; }
    public Long getRedeemed() { return redeemed; }
    public void setRedeemed(Long redeemed) { this.redeemed = redeemed; }
    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
}
