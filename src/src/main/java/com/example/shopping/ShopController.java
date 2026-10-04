package com.example.shopping;

import java.security.Principal;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopController {
    private final ShopRepository shop;
    private final PasswordEncoder encoder;
    ShopController(ShopRepository shop, PasswordEncoder encoder) { this.shop = shop; this.encoder = encoder; }

    @GetMapping("/") String home(Model model, Principal user) {
        model.addAttribute("products", shop.products());
        model.addAttribute("username", user == null ? null : user.getName());
        return "home";
    }
    @GetMapping("/login") String login() { return "login"; }
    @GetMapping("/register") String register() { return "register"; }
    @PostMapping("/register") String registerUser(@RequestParam String username, @RequestParam String password,
                                                     Model model) {
        username = username.trim();
        if (!username.matches("[A-Za-z0-9_]{3,40}") || password.length() < 8 || password.length() > 72) {
            model.addAttribute("error", "Username: 3–40 letters, digits or _. Password: 8–72 characters.");
            return "register";
        }
        try {
            shop.register(username, encoder.encode(password));
            return "redirect:/login?registered";
        } catch (DuplicateKeyException ex) {
            model.addAttribute("error", "That username is taken.");
            return "register";
        }
    }
    @PostMapping("/cart/add") String add(@RequestParam long productId, Principal user) {
        try { shop.add(user.getName(), productId); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        return "redirect:/cart";
    }
    @PostMapping("/cart/remove") String remove(@RequestParam long productId, Principal user) {
        shop.remove(user.getName(), productId);
        return "redirect:/cart";
    }
    @GetMapping("/cart") String cart(Model model, Principal user) {
        var lines = shop.cart(user.getName());
        model.addAttribute("lines", lines);
        model.addAttribute("total", String.format(java.util.Locale.US, "$%.2f",
                lines.stream().mapToLong(ShopRepository.CartLine::lineCents).sum() / 100.0));
        return "cart";
    }
    @GetMapping("/checkout") String checkout(Model model, Principal user) {
        var lines = shop.cart(user.getName());
        if (lines.isEmpty()) return "redirect:/cart";
        model.addAttribute("total", String.format(java.util.Locale.US, "$%.2f",
                lines.stream().mapToLong(ShopRepository.CartLine::lineCents).sum() / 100.0));
        return "checkout";
    }
    @PostMapping("/checkout") String placeOrder(@RequestParam String recipient, @RequestParam String address,
                                                  @RequestParam String city, Principal user, RedirectAttributes flash) {
        recipient = recipient.trim(); address = address.trim(); city = city.trim();
        if (recipient.isBlank() || recipient.length() > 120 || address.isBlank() || address.length() > 250 ||
                city.isBlank() || city.length() > 120) {
            flash.addFlashAttribute("error", "Please enter a valid name, address and city.");
            return "redirect:/checkout";
        }
        try { flash.addFlashAttribute("notice", "Demo order #" + shop.checkout(user.getName(), recipient, address, city) + " saved."); }
        catch (IllegalStateException ex) { return "redirect:/cart"; }
        return "redirect:/orders";
    }
    @GetMapping("/orders") String orders(Model model, Principal user) {
        model.addAttribute("orders", shop.orders(user.getName()));
        return "orders";
    }
}
