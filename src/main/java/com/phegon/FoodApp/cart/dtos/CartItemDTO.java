package com.phegon.FoodApp.cart.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegon.FoodApp.menu.dtos.MenuDTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDTO {
    private Long id; // Unique identifier for the cart item

    private MenuDTO menu; // Menu item associated with the cart item

    private int quantity; // Quantity of the menu item in the cart

    private BigDecimal pricePerUnit;

    private BigDecimal subTotal; // Total price for the cart item (quantity * pricePerUnit)
}
