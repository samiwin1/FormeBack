package tn.esprit.shop.shopservice.modules.cart.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.cart.repository.CartItemRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CartItemService implements ICartItemService {

    CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem addCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem getCartItemBy(long id) {
        return cartItemRepository.findById(id).orElse(null);
    }

    @Override
    public CartItem updateCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteCartItem(long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public List<CartItem> addListCartItems(List<CartItem> cartItems) {
        return cartItemRepository.saveAll(cartItems);
    }

    @Override
    public List<CartItem> findByCartId(Long cartId) {
        return cartItemRepository.findByCart_IdCart(cartId);
    }

    @Override
    public CartItem findByCartIdAndProductId(Long cartId, Long productId) {
        return cartItemRepository.findByCart_IdCartAndProduct_IdProduct(cartId, productId);
    }
}
