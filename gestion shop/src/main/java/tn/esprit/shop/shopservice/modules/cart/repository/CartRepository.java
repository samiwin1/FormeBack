package tn.esprit.shop.shopservice.modules.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findFirstByUserIdAndStatusOrderByIdCartDesc(Long userId, String status);
}
