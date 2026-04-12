package tn.esprit.shop.shopservice.modules.product.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.product.entity.Product;
import tn.esprit.shop.shopservice.modules.product.service.IProductService;

import java.util.List;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {

    IProductService iproductservice;

    @GetMapping("/listProducts")
    public List<Product> listProducts() {
        return iproductservice.getAllProducts();
    }

    @PostMapping("/addProduct")
    public Product addProduct(@RequestBody Product product) {
        return iproductservice.addProduct(product);
    }

    @GetMapping("/getProduct/{id}")
    public Product getProduct(@PathVariable long id) {
        return iproductservice.getProductBy(id);
    }

    @PutMapping("/updateProduct/{id}")
    public Product updateProduct(@PathVariable long id, @RequestBody Product product) {
        product.setIdProduct(id);
        return iproductservice.updateProduct(product);
    }

    @DeleteMapping("/deleteProduct/{id}")
    public void deleteProduct(@PathVariable long id) {
        iproductservice.deleteProduct(id);
    }

    @PostMapping("/addListProducts")
    public List<Product> addListProducts(@RequestBody List<Product> products) {
        return iproductservice.addListProduct(products);
    }

    @GetMapping("/getByFormationId/{formationId}")
    public Product getByFormationId(@PathVariable Long formationId) {
        return iproductservice.findByFormationId(formationId);
    }
}
