package com.phegon.FoodApp.cart.controller;

import com.phegon.FoodApp.cart.dtos.CartDTO;
import com.phegon.FoodApp.cart.services.CartService;
import com.phegon.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<Response<?>> addItemToCart(@RequestBody CartDTO cartDTO) {
        return ResponseEntity.ok(cartService.addItemToCart(cartDTO));
    }

    @PutMapping("/items/increment/{menuId}")
    public ResponseEntity<Response<?>> incrementItem(@PathVariable Long menuId) {
        return ResponseEntity.ok(cartService.incrementItem(menuId));
    }

    @PutMapping("/items/decrement/{menuId}")
    public ResponseEntity<Response<?>> decrementItem(@PathVariable Long menuId) {
        return ResponseEntity.ok(cartService.decrementItem(menuId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Response<?>> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }

    @GetMapping
    public ResponseEntity<Response<CartDTO>> getShoppingCart() {
        return ResponseEntity.ok(cartService.getShoppingCart());
    }

    @DeleteMapping
    public ResponseEntity<Response<?>> clearShoppingCart() {
        return ResponseEntity.ok(cartService.clearShoppingCart());
    }

}
