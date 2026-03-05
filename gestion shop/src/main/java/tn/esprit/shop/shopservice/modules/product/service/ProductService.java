package tn.esprit.shop.shopservice.modules.product.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.formation.repository.FormationRepository;
import tn.esprit.shop.shopservice.modules.product.entity.Product;
import tn.esprit.shop.shopservice.modules.product.repository.ProductRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService implements IProductService {

    ProductRepository productRepository;
    FormationRepository formationRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product addProduct(Product product) {
        Formation formation = formationRepository.findById(product.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation not found with id: " + product.getFormation().getId()));
        product.setFormation(formation);
        return productRepository.save(product);
    }

    @Override
    public Product getProductBy(long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product updateProduct(Product product) {
        Formation formation = formationRepository.findById(product.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation not found with id: " + product.getFormation().getId()));
        product.setFormation(formation);
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> addListProduct(List<Product> products) {
        return productRepository.saveAll(products);
    }

    @Override
    public Product findByFormationId(Long formationId) {
        return productRepository.findByFormation_Id(formationId);
    }
}
