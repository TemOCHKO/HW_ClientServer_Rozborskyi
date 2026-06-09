package org.temochko.Repositories;

import org.temochko.DTOs.ProductCreateDto;
import org.temochko.DTOs.ProductUpdateDto;
import org.temochko.Models.Product;
import org.temochko.Models.ProductCriteria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MySqlProductRepository implements IProductRepository {

    private Connection connection;

    public MySqlProductRepository(String dbUrl, String username, String password) {
        try {
            this.connection = DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Can't connect to database: " + e.getMessage());
        }

        init();
    }

    @Override
    public int insert(ProductCreateDto product) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO product(product_name, price, quantity) values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());

            int inserted = ps.executeUpdate();
            if (inserted < 1) {
                throw new RuntimeException("Insert failed");
            }

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            throw new RuntimeException("Insert failed");
        } catch (SQLException e) {
            throw new RuntimeException("Can't insert product: " + product, e);
        }
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public List<Product> getAll(ProductCriteria criteria) {

        StringBuilder sb = new StringBuilder("select * from product");
        ArrayList<Object> params = new ArrayList<>();

        String filterPart = Stream.of(stringEquals("product_name", criteria.getName(), params),
                        stringEquals("price", criteria.getPrice() + "", params),
                        stringEquals("faculty", criteria.getQuantity() + "", params))

                .filter(s -> s != null)
                .collect(Collectors.joining(" and "));

        if (!filterPart.isEmpty()) {
            sb.append(" where ").append(filterPart);
        }

        System.out.println(sb.toString());
        System.out.println(params);

        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i+1, params.get(i));
            }

            List<Product> products = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(rs.getInt("id"), rs.getString("product_name"), rs.getDouble("price"), rs.getInt("quantity")));
                }
            }

            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Can't get products", e);
        }
    }

    @Override
    public Optional<Product> getById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("select * from product where id = ?")) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Product(rs.getInt("id"), rs.getString("product_name"), rs.getDouble("price"), rs.getInt("quantity")));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Can't get product by id: " + id, e);
        }
    }

    @Override
    public int deleteAll() {
        try (PreparedStatement ps = connection.prepareStatement("delete from product")) {
            int numberOfRows = ps.executeUpdate();
            return numberOfRows;
        } catch (SQLException e) {
            throw new RuntimeException("Can't delete products", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("delete from product where id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(ProductUpdateDto product) {
        try (PreparedStatement ps = connection.prepareStatement("update product SET product_name = ?, price = ?, quantity = ? where id = ?")) {
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setInt(4, product.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS product (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    product_name VARCHAR(30) not null,
                    price double(10, 2) not null,
                    quantity int(11) not null
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Exception while DB init", e);
        }
    }

    private static String stringEquals(String columnName, String value, List<Object> params) {
        if (value == null) {
            return null;
        }

        params.add(value);
        return columnName + " = ?";
    }

}
