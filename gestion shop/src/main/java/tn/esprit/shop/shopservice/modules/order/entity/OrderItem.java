package tn.esprit.shop.shopservice.modules.order.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.product.entity.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrderItem;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_formation"))
    private Formation formation;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPriceSnapshot;

    @Column(nullable = false)
    private String formationTitleSnapshot;
}
