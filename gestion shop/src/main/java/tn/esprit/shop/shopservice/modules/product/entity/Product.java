package tn.esprit.shop.shopservice.modules.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
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

    @OneToOne(optional = false)
    @JoinColumn(name = "formation_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_product_formation"))
    private Formation formation;

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
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
