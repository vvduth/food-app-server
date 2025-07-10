package com.phegon.FoodApp.cart.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "cart_item")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart; // Assuming CartItem is associated with a Cart

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private int quantity; // Quantity of the item in the cart

    private BigDecimal pricePerUnit; // Price of the item in the cart

    private BigDecimal subTotal; // Total price for the quantity of the item in the cart
}
