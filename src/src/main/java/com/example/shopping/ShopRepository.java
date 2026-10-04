package com.example.shopping;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ShopRepository {
    private final JdbcTemplate db;
    ShopRepository(JdbcTemplate db) { this.db = db; }

    public record Product(long id, String name, String description, int priceCents) {
        public String price() { return String.format(java.util.Locale.US, "$%.2f", priceCents / 100.0); }
    }
    public record CartLine(long id, String name, int priceCents, int quantity) {
        public long lineCents() { return (long) priceCents * quantity; }
        public String price() { return String.format(java.util.Locale.US, "$%.2f", priceCents / 100.0); }
        public String linePrice() { return String.format(java.util.Locale.US, "$%.2f", lineCents() / 100.0); }
    }
    public record Order(long id, long totalCents, String createdAt) {
        public String total() { return String.format(java.util.Locale.US, "$%.2f", totalCents / 100.0); }
    }

    public List<Product> products() {
        return db.query("SELECT id, name, description, price_cents FROM products ORDER BY id",
            (rs, row) -> new Product(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
    }
    public void register(String username, String encodedPassword) throws DuplicateKeyException {
        db.update("INSERT INTO shop_users(username, password_hash) VALUES (?, ?)", username, encodedPassword);
    }
    private long userId(String username) {
        return db.queryForObject("SELECT id FROM shop_users WHERE username = ?", Long.class, username);
    }
    @Transactional
    public void add(String username, long productId) {
        long userId = userId(username);
        db.queryForObject("SELECT id FROM shop_users WHERE id = ? FOR UPDATE", Long.class, userId);
        int changed = db.update("INSERT INTO cart_items(user_id, product_id, quantity) " +
                "SELECT ?, id, 1 FROM products WHERE id = ? " +
                "ON CONFLICT(user_id, product_id) DO UPDATE SET quantity = LEAST(cart_items.quantity + 1, 99)", userId, productId);
        if (changed == 0) throw new IllegalArgumentException("Product not found");
    }
    @Transactional
    public void remove(String username, long productId) {
        long userId = userId(username);
        db.queryForObject("SELECT id FROM shop_users WHERE id = ? FOR UPDATE", Long.class, userId);
        db.update("DELETE FROM cart_items WHERE user_id = ? AND product_id = ?", userId, productId);
    }
    public List<CartLine> cart(String username) {
        return db.query("SELECT p.id, p.name, p.price_cents, c.quantity FROM cart_items c " +
                "JOIN products p ON p.id = c.product_id WHERE c.user_id = ? ORDER BY p.id",
            (rs, row) -> new CartLine(rs.getLong(1), rs.getString(2), rs.getInt(3), rs.getInt(4)), userId(username));
    }
    public List<Order> orders(String username) {
        return db.query("SELECT id, total_cents, created_at FROM shop_orders WHERE user_id = ? ORDER BY id DESC",
            (rs, row) -> new Order(rs.getLong(1), rs.getLong(2), rs.getTimestamp(3).toString()), userId(username));
    }
    @Transactional
    public long checkout(String username, String recipient, String address, String city) {
        long userId = userId(username);
        // Serialize checkout requests for this user, then read the cart in the same transaction.
        db.queryForObject("SELECT id FROM shop_users WHERE id = ? FOR UPDATE", Long.class, userId);
        List<CartLine> lines = cart(username);
        if (lines.isEmpty()) throw new IllegalStateException("Your cart is empty");
        long total = lines.stream().mapToLong(CartLine::lineCents).sum();
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        db.update(connection -> {
            var stmt = connection.prepareStatement(
                "INSERT INTO shop_orders(user_id, recipient, address, city, total_cents) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"});
            stmt.setLong(1, userId);
            stmt.setString(2, recipient);
            stmt.setString(3, address);
            stmt.setString(4, city);
            stmt.setLong(5, total);
            return stmt;
        }, key);
        long orderId = key.getKey().longValue();
        for (CartLine line : lines) {
            db.update("INSERT INTO order_items(order_id, product_id, name, unit_price_cents, quantity) VALUES (?, ?, ?, ?, ?)",
                orderId, line.id(), line.name(), line.priceCents(), line.quantity());
        }
        db.update("DELETE FROM cart_items WHERE user_id = ?", userId);
        return orderId;
    }
}
