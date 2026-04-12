package tn.esprit.shop.shopservice.modules.cart.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.cart.entity.Cart;
import tn.esprit.shop.shopservice.modules.cart.service.ICartService;

import java.util.List;

@RestController
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {

    ICartService icartservice;

    @GetMapping("/listCarts")
    public List<Cart> listCarts() {
        return icartservice.getAllCarts();
    }

    @PostMapping("/addCart")
    public Cart addCart(@RequestBody Cart cart) {
        return icartservice.addCart(cart);
    }

    @GetMapping("/getCart/{id}")
    public Cart getCart(@PathVariable long id) {
        return icartservice.getCartBy(id);
    }

    @PutMapping("/updateCart/{id}")
    public Cart updateCart(@PathVariable long id, @RequestBody Cart cart) {
        cart.setIdCart(id);
        return icartservice.updateCart(cart);
    }

    @DeleteMapping("/deleteCart/{id}")
    public void deleteCart(@PathVariable long id) {
        icartservice.deleteCart(id);
    }

    @PostMapping("/addListCarts")
    public List<Cart> addListCarts(@RequestBody List<Cart> carts) {
        return icartservice.addListCart(carts);
    }

    @GetMapping("/getActiveCartByUser/{userId}")
    public Cart getActiveCartByUser(@PathVariable Long userId) {
        return icartservice.findActiveCartByUserId(userId);
    }
}
