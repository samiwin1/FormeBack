package tn.esprit.shop.shopservice.modules.cart.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tn.esprit.shop.shopservice.modules.cart.entity.CartItem;
import tn.esprit.shop.shopservice.modules.cart.repository.CartItemRepository;
import tn.esprit.shop.shopservice.modules.product.entity.Product;
import tn.esprit.shop.shopservice.modules.product.repository.ProductRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CartItemService implements ICartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @Override
    public CartItem addCartItem(CartItem cartItem) {
        Product requestProduct = cartItem.getProduct();
        if (requestProduct == null || requestProduct.getIdProduct() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "product.idProduct is required");
        }

        Product persistedProduct = productRepository.findById(requestProduct.getIdProduct())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with id: " + requestProduct.getIdProduct()));

        cartItem.setProduct(persistedProduct);

        if (cartItem.getFormationId() == null) {
            cartItem.setFormationId(persistedProduct.getFormationId());
        }

        String resolvedTitle = persistedProduct.getFormationTitleSnapshot();
        if (resolvedTitle == null || resolvedTitle.isBlank()) {
            resolvedTitle = persistedProduct.getTitleSnapshot();
        }
        if (resolvedTitle == null || resolvedTitle.isBlank()) {
            resolvedTitle = "Formation #" + persistedProduct.getFormationId();
        }
        cartItem.setFormationTitleSnapshot(resolvedTitle);
        cartItem.setTitleSnapshot(resolvedTitle);

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
