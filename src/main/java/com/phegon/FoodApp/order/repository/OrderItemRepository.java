package com.phegon.FoodApp.order.repository;

import com.phegon.FoodApp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Additional query methods can be defined here if needed

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
              "FROM OrderItem oi " +
                "WHERE oi.order.id = :orderId AND oi.menu.id = :menuId"
    )
    boolean existsByOrderIdAndMenuId(
            @Param("orderId") Long orderId, // need @param to specify the parameter name for the query
            @Param("menuId") Long menuId
    );
}
