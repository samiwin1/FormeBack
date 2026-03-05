package tn.esprit.shop.shopservice.modules.product.dto;

public record ProductRequest(
        Long formationId,
        Integer price,
        String currency,
        Boolean isAvailable
) {}
