package com.phegon.FoodApp.cart.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// DTO is a Data Transfer Object that represents the Cart entity. use to transfer data between the application and the client.
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDTO {

    private Long id;
    private List<CartItemDTO> cartItems; // List of items in the cart
    private Long menuId; // ID of the menu associated with the cart
    private int quantity; // Total quantity of items in the cart
    private BigDecimal totalAmount;
}
