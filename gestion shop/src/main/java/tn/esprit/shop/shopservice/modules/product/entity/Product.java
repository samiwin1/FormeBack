package tn.esprit.shop.shopservice.modules.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.order.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cartItems", "orderItems"})
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduct;

    /** Id of the formation in the external formation-service (no FK in this DB). */
    @Column(name = "formation_id", nullable = false, unique = true)
    private Long formationId;

    /** Snapshot of formation title for display (from formation-service at product creation). */
    @Column(name = "formation_title_snapshot", nullable = false)
    private String formationTitleSnapshot;

    /** Legacy snapshot column kept for backward compatibility with older DB schema. */
    @Column(name = "title_snapshot", nullable = false)
    private String titleSnapshot;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String currency;

    private Boolean isAvailable;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<OrderItem> orderItems;

    @PrePersist
    public void beforeSave() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAvailable == null) isAvailable = true;
        if (titleSnapshot == null) titleSnapshot = formationTitleSnapshot;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        if (titleSnapshot == null) titleSnapshot = formationTitleSnapshot;
    }
}
