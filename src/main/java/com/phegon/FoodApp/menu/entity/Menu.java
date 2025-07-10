package com.phegon.FoodApp.menu.entity;


import com.phegon.FoodApp.category.entity.Category;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false) // link to category entity
    private Category category;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<Review> reviews;

}
