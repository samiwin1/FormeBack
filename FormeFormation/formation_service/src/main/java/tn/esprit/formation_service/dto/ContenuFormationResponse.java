package tn.esprit.formation_service.dto;

public class ContenuFormationResponse {

    private Long id;
    private Long formation_id;
    private String title;
    private String content_type;
    private String content_body;
    private Integer order_index;
    private Long evaluation_id;
    private Boolean is_locked;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFormation_id() { return formation_id; }
    public void setFormation_id(Long formation_id) { this.formation_id = formation_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent_type() { return content_type; }
    public void setContent_type(String content_type) { this.content_type = content_type; }
    public String getContent_body() { return content_body; }
    public void setContent_body(String content_body) { this.content_body = content_body; }
    public Integer getOrder_index() { return order_index; }
    public void setOrder_index(Integer order_index) { this.order_index = order_index; }
    public Long getEvaluation_id() { return evaluation_id; }
    public void setEvaluation_id(Long evaluation_id) { this.evaluation_id = evaluation_id; }
    public Boolean getIs_locked() { return is_locked; }
    public void setIs_locked(Boolean is_locked) { this.is_locked = is_locked; }
}
