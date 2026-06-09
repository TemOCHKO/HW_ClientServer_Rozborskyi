package org.temochko.Services;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;
import org.temochko.Repositories.IProductRepository;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

public class ProductService implements IProductService {
    private IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public int createProduct(ProductCreateDto product) {
        var errors = Validator.validate(product);
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var error : errors)
                sb.append(error.errorMessage()).append("\n");
            throw new InvalidParameterException(sb.toString());
        }
        return productRepository.insert(product);
    }

    @Override
    public int getCountOfProducts() {
        return productRepository.count();
    }

    @Override
    public List<Product> getAllProducts(ProductCriteria criteria) {
        return productRepository.getAll(criteria);
    }

    @Override
    public Optional<Product> getProductById(int id) {
        return productRepository.getById(id);
    }

    @Override
    public int deleteAllProducts() {
        return productRepository.deleteAll();
    }

    @Override
    public boolean deleteProductById(int id) {
        return productRepository.deleteById(id);
    }

    @Override
    public boolean updateProduct(ProductUpdateDto product) {
        return productRepository.update(product);
    }
}
