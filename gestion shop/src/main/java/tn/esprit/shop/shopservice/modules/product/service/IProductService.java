package tn.esprit.shop.shopservice.modules.product.service;

import tn.esprit.shop.shopservice.modules.product.entity.Product;

import java.util.List;

public interface IProductService {

    List<Product> getAllProducts();

    Product addProduct(Product product);

    Product getProductBy(long id);

    Product updateProduct(Product product);

    void deleteProduct(long id);

    List<Product> addListProduct(List<Product> products);

    Product findByFormationId(Long formationId);
}
