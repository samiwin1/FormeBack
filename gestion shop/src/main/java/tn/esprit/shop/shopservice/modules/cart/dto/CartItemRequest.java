package tn.esprit.shop.shopservice.modules.cart.dto;

public record CartItemRequest(
        Long cartId,
        Long productId,
        Long formationId,
        Integer quantity
) {}
