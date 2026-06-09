package org.temochko;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.temochko.DTOs.ProductCreateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;
import org.temochko.Repositories.MySqlProductRepository;
import org.temochko.Services.ProductService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceIntegrationTest {

    private ProductService productService;
    private MySqlProductRepository realRepository;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    @BeforeEach
    void setUp() {
        realRepository = new MySqlProductRepository(DB_URL, DB_USER, DB_PASSWORD);
        productService = new ProductService(realRepository);
        productService.deleteAllProducts();
    }

    @Test
    void getAllProducts_ShouldReturnDataFromDatabase() {
        // Arrange
        productService.createProduct(new ProductCreateDto("Mouse", 25.0, 50));
        productService.createProduct(new ProductCreateDto("Monitor", 300.0, 10));
        productService.createProduct(new ProductCreateDto("USB Cable", 15.0, 100));

        // Act
        List<Product> products = productService.getAllProducts(new ProductCriteria());

        // Assert
        assertEquals(3, products.size(), "Should retrieve exactly 3 products from the DB");
    }

    @Test
    void addStock_ShouldUpdateDatabaseRecord() {
        // Arrange
        int productId = productService.createProduct(new ProductCreateDto("Desk", 250.0, 5));

        // Act
        boolean result = productService.addStock(productId, 10);

        // Assert
        assertTrue(result);

        Product updatedProduct = productService.getProductById(productId);
        assertEquals(15, updatedProduct.getQuantity(), "stock should be 15");
    }

    @Test
    void deleteProductById_ShouldRemoveFromDatabase() {
        // Arrange
        int productId = productService.createProduct(new ProductCreateDto("Webcam", 60.0, 20));

        assertEquals(1, productService.getCountOfProducts());

        // Act
        boolean result = productService.deleteProductById(productId);

        // Assert
        assertTrue(result);
        assertEquals(0, productService.getCountOfProducts(), "Database should be empty after deletion");
        assertNull(productService.getProductById(productId), "Fetching deleted product should return null");
    }

    @Test
    void getCountOfProducts_ShouldCountCorrectlyInDatabase() {
        // Arrange
        assertEquals(0, productService.getCountOfProducts());

        // Act
        productService.createProduct(new ProductCreateDto("Laptop", 1000.0, 5));
        productService.createProduct(new ProductCreateDto("Charger", 50.0, 20));

        // Assert
        assertEquals(2, productService.getCountOfProducts(), "SQL COUNT(*) should return 2");
    }
}