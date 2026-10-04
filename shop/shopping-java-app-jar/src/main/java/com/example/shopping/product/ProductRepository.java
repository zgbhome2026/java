package com.example.shopping.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList(
                "SELECT id, sku, name, price, stock FROM products WHERE active = TRUE ORDER BY id"
        );
    }

    public Map<String, Object> get(long id) {
        var rows = jdbc.queryForList(
                "SELECT id, sku, name, price, stock FROM products WHERE id = ? AND active = TRUE",
                id
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        return rows.get(0);
    }
}
