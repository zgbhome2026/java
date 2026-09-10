package com.example.checkout.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ShoppingController {

    // Mock product catalog data
    private static final List<Product> PRODUCTS = List.of(
        new Product("P1", "Wireless Mouse", new BigDecimal("29.99")),
        new Product("P2", "Mechanical Keyboard", new BigDecimal("89.99")),
        new Product("P3", "USB-C Hub", new BigDecimal("19.99"))
    );

    // In-memory cart for demonstration
    private final List<CartItem> cart = new ArrayList<>();

    @GetMapping("/store")
    public String viewStore(Model model) {
        model.addAttribute("products", PRODUCTS);
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", calculateTotal());
        return "store";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String productId, @RequestParam int quantity) {
        Product found = PRODUCTS.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (found != null) {
            cart.add(new CartItem(found.getId(), found.getName(), found.getPrice(), quantity));
        }
        return "redirect:/store";
    }

    private BigDecimal calculateTotal() {
        return cart.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

class Product {
    private String id;
    private String name;
    private BigDecimal price;

    public Product(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
}