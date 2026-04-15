package tn.esprit.partnerperformance.dto;

public class LeaderboardRow {
    private Integer rank;
    private Long partnerId;
    private String partnerName;
    private Double value;

    public LeaderboardRow() {}

    public LeaderboardRow(Integer rank, Long partnerId, String partnerName, Double value) {
        this.rank = rank;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.value = value;
    }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
