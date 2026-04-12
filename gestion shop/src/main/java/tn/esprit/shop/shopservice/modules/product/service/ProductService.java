package tn.esprit.shop.shopservice.modules.product.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.shop.shopservice.client.FormationClient;
import tn.esprit.shop.shopservice.client.FormationResponse;
import tn.esprit.shop.shopservice.modules.product.entity.Product;
import tn.esprit.shop.shopservice.modules.product.repository.ProductRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final FormationClient formationClient;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product addProduct(Product product) {
        Product existing = productRepository.findByFormationId(product.getFormationId());
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A product already exists for formation id: " + product.getFormationId());
        }
        FormationResponse formation = formationClient.getFormationById(product.getFormationId());
        if (formation == null) {
            throw new RuntimeException("Formation not found with id: " + product.getFormationId() + ". Ensure formation-service is running and the formation exists.");
        }
        product.setFormationTitleSnapshot(formation.getTitle());
        product.setTitleSnapshot(formation.getTitle());
        return productRepository.save(product);
    }

    @Override
    public Product getProductBy(long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product updateProduct(Product product) {
        Product existing = productRepository.findByFormationId(product.getFormationId());
        if (existing != null && !existing.getIdProduct().equals(product.getIdProduct())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another product already exists for formation id: " + product.getFormationId());
        }
        FormationResponse formation = formationClient.getFormationById(product.getFormationId());
        if (formation == null) {
            throw new RuntimeException("Formation not found with id: " + product.getFormationId());
        }
        product.setFormationTitleSnapshot(formation.getTitle());
        product.setTitleSnapshot(formation.getTitle());
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
        return productRepository.findByFormationId(formationId);
    }
}
