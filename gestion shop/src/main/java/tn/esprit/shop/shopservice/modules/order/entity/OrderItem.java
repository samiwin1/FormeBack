package tn.esprit.shop.shopservice.modules.order.entity;

import jakarta.persistence.*;
import lombok.*;
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
}
