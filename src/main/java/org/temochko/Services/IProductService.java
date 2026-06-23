package org.temochko.Services;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    int createProduct(ProductCreateDto product);
    int getCountOfProducts();
    List<Product> getAllProducts(ProductCriteria criteria);
    Product getProductById(int id);
    int deleteAllProducts();
    boolean deleteProductById(int id);
    boolean updateProduct(ProductUpdateDto product);
    boolean addStock(int idProd, int stock);
    boolean deleteStock(int idProd, int stock);
    boolean nameExists(String name);
}
