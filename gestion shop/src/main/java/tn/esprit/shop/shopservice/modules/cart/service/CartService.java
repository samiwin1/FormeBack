package tn.esprit.shop.shopservice.modules.cart.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.cart.entity.Cart;
import tn.esprit.shop.shopservice.modules.cart.repository.CartRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CartService implements ICartService {

    CartRepository cartRepository;

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public Cart addCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCartBy(long id) {
        return cartRepository.findById(id).orElse(null);
    }

    @Override
    public Cart updateCart(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public void deleteCart(long id) {
        cartRepository.deleteById(id);
    }

    @Override
    public List<Cart> addListCart(List<Cart> carts) {
        return cartRepository.saveAll(carts);
    }

    @Override
    public Cart findActiveCartByUserId(Long userId) {
        return cartRepository.findFirstByUserIdAndStatusOrderByIdCartDesc(userId, "ACTIVE");
    }
}
