package com.ecommerce.product.service;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }

    public Product createProduct(Product product) {
    	System.out.println(product.getName()+product.getStock());
        return repository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Optional<Product> existing = repository.findById(id);
        if (existing.isPresent()) {
            Product updated = existing.get();
            updated.setName(product.getName());
            updated.setDescription(product.getDescription());
            updated.setPrice(product.getPrice());
            updated.setStock(product.getStock());
            return repository.save(updated);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    public void deleteProduct(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }
}