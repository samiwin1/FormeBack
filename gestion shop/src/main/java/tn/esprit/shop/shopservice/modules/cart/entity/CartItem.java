package tn.esprit.shop.shopservice.modules.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.product.entity.Product;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartItem;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Id of the formation in the external formation-service (reference only). */
    @Column(name = "formation_id", nullable = false)
    private Long formationId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPriceSnapshot;

    @Column(nullable = false)
    private String formationTitleSnapshot;

    /** Legacy snapshot column kept for backward compatibility with older DB schema. */
    @Column(name = "title_snapshot", nullable = false)
    private String titleSnapshot;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeSave() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantity == null) quantity = 1;
        if (titleSnapshot == null || titleSnapshot.isBlank()) {
            titleSnapshot = formationTitleSnapshot;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        if (titleSnapshot == null || titleSnapshot.isBlank()) {
            titleSnapshot = formationTitleSnapshot;
        }
    }
}
