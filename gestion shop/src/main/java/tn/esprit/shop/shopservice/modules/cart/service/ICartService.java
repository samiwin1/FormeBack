package tn.esprit.shop.shopservice.modules.cart.service;

import tn.esprit.shop.shopservice.modules.cart.entity.Cart;

import java.util.List;

public interface ICartService {

    List<Cart> getAllCarts();

    Cart addCart(Cart cart);

    Cart getCartBy(long id);

    Cart updateCart(Cart cart);

    void deleteCart(long id);

    List<Cart> addListCart(List<Cart> carts);

    Cart findActiveCartByUserId(Long userId);
}
