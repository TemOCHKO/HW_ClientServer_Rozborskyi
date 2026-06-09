package org.temochko.Repositories;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;

import java.util.List;
import java.util.Optional;

public interface IProductRepository {
    int insert(ProductCreateDto product);
    int count();
    List<Product> getAll(ProductCriteria criteria);
    Product getById(int id);
    int deleteAll();
    boolean deleteById(int id);
    boolean update(ProductUpdateDto product);
}
