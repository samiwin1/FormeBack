package tn.esprit.shop.shopservice.modules.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_IdCart(Long cartId);
    CartItem findByCart_IdCartAndProduct_IdProduct(Long cartId, Long productId);
}
