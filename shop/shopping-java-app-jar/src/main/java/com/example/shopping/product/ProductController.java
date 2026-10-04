package com.example.shopping.product;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return products.list();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable long id) {
        return products.get(id);
    }
}
