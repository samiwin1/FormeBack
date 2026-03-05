package tn.esprit.shop.shopservice.modules.cart.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.cart.service.ICartItemService;

import java.util.List;

@RestController
@RequestMapping("/cartItem")
@AllArgsConstructor
public class CartItemController {

    ICartItemService icartItemservice;

    @GetMapping("/listCartItems")
    public List<CartItem> listCartItems() {
        return icartItemservice.getAllCartItems();
    }

    @PostMapping("/addCartItem")
    public CartItem addCartItem(@RequestBody CartItem cartItem) {
        return icartItemservice.addCartItem(cartItem);
    }

    @GetMapping("/getCartItem/{id}")
    public CartItem getCartItem(@PathVariable long id) {
        return icartItemservice.getCartItemBy(id);
    }

    @PutMapping("/updateCartItem/{id}")
    public CartItem updateCartItem(@PathVariable long id, @RequestBody CartItem cartItem) {
        cartItem.setIdCartItem(id);
        return icartItemservice.updateCartItem(cartItem);
    }

    @DeleteMapping("/deleteCartItem/{id}")
    public void deleteCartItem(@PathVariable long id) {
        icartItemservice.deleteCartItem(id);
    }

    @PostMapping("/addListCartItems")
    public List<CartItem> addListCartItems(@RequestBody List<CartItem> cartItems) {
        return icartItemservice.addListCartItems(cartItems);
    }

    @GetMapping("/getByCartId/{cartId}")
    public List<CartItem> getByCartId(@PathVariable Long cartId) {
        return icartItemservice.findByCartId(cartId);
    }

    @GetMapping("/getByCartIdAndProductId/{cartId}/{productId}")
    public CartItem getByCartIdAndProductId(@PathVariable Long cartId, @PathVariable Long productId) {
        return icartItemservice.findByCartIdAndProductId(cartId, productId);
    }
}
