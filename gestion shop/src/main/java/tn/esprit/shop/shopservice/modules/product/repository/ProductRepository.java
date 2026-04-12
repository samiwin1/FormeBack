package tn.esprit.shop.shopservice.modules.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.shop.shopservice.modules.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByFormationId(Long formationId);
}
