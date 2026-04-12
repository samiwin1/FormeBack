package tn.esprit.shop.shopservice.modules.cart.service;

import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;

import java.util.List;

public interface ICartItemService {

    List<CartItem> getAllCartItems();

    CartItem addCartItem(CartItem cartItem);

    CartItem getCartItemBy(long id);

    CartItem updateCartItem(CartItem cartItem);

    void deleteCartItem(long id);

    List<CartItem> addListCartItems(List<CartItem> cartItems);

    List<CartItem> findByCartId(Long cartId);

    CartItem findByCartIdAndProductId(Long cartId, Long productId);
}
