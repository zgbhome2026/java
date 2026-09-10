package com.example.checkout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> processCheckout(@RequestBody OrderRequest request) {
        // Validate cart and calculate total
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mock payment processing logic
        String orderId = "ORD-" + System.currentTimeMillis();
        boolean paymentSuccess = totalAmount.compareTo(BigDecimal.ZERO) > 0;

        if (paymentSuccess) {
            return ResponseEntity.ok(new OrderResponse(orderId, "SUCCESS", totalAmount));
        } else {
            return ResponseEntity.badRequest().body(new OrderResponse(orderId, "FAILED", totalAmount));
        }
    }
}

class OrderRequest {
    private List<CartItem> items;
    private String customerEmail;

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}

class CartItem {
    private String productId;
    private int quantity;
    private BigDecimal price;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

class OrderResponse {
    private String orderId;
    private String status;
    private BigDecimal totalAmount;

    public OrderResponse(String orderId, String status, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}