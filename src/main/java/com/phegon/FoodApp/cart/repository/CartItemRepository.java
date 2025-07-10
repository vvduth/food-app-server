package com.phegon.FoodApp.cart.repository;

import com.phegon.FoodApp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Additional query methods can be defined here if needed
    // For example, to find all items in a specific cart:
    // List<CartItem> findByCartId(Long cartId);
}
