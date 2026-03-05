package tn.esprit.shop.shopservice.modules.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "formation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_formation"))
    private Formation formation;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPriceSnapshot;

    @Column(nullable = false)
    private String formationTitleSnapshot;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforeSave() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantity == null) quantity = 1;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
