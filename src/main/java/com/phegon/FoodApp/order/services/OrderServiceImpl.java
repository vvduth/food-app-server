package com.phegon.FoodApp.order.services;

import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.auth_users.services.UserService;
import com.phegon.FoodApp.cart.entity.Cart;
import com.phegon.FoodApp.cart.entity.CartItem;
import com.phegon.FoodApp.cart.repository.CartRepository;
import com.phegon.FoodApp.cart.services.CartService;
import com.phegon.FoodApp.email_notification.dtos.NotificationDTO;
import com.phegon.FoodApp.email_notification.services.NotificationService;
import com.phegon.FoodApp.enums.OrderStatus;
import com.phegon.FoodApp.enums.PaymentStatus;
import com.phegon.FoodApp.exceptions.BadRequestException;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.menu.dtos.MenuDTO;
import com.phegon.FoodApp.order.dtos.OrderDTO;
import com.phegon.FoodApp.order.dtos.OrderItemDTO;
import com.phegon.FoodApp.order.entity.Order;
import com.phegon.FoodApp.order.entity.OrderItem;
import com.phegon.FoodApp.order.repository.OrderItemRepository;
import com.phegon.FoodApp.order.repository.OrderRepository;
import com.phegon.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final CartService cartService;
    private final CartRepository    cartRepository;

    @Value("${base.payment.link}")
    private String basePaymentLink;

    @Transactional
    @Override
    public Response<?> placeOrderFromCart() {
        log.info("Placing order from cart...");

        User customer = userService.getCurrentLoggedInUser();
        String deliveryAddress = customer.getAddress();

        if (deliveryAddress == null ){
            throw new NotFoundException("Delivery address not found for user: " + customer.getName());
        }

        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + customer.getName()));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty for user: " + customer.getName());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getPricePerUnit())
                    .subTotal(cartItem.getSubTotal())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
        }

        Order order = Order.builder()
                .user(customer)
                .orderItems(orderItems)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);


        // clear the cart after placing the order
        cartService.clearShoppingCart();

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        // send email notifications
        sendOrderConfirmationEmail(customer, orderDTO);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order placed successfully")
                .data(orderDTO)
                .build();
    }

    @Override
    public Response<OrderDTO> getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + orderId));

        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order fetched successfully")
                .data(orderDTO)
                .build();
    }

    @Override
    public Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info("Fetching all orders with status: {}, page: {}, size: {}", orderStatus, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orderPage;

        if (orderStatus != null) {
            orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }

        Page<OrderDTO> orderDTOPage = orderPage.map(order -> {
            OrderDTO dto = modelMapper.map(order, OrderDTO.class);
            dto.getOrderItems().forEach(orderItemDTO -> orderItemDTO.getMenu().setReviews(null)); // Avoid loading reviews in DTO
            return dto;
        });

        return Response.<Page<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders fetched successfully")
                .data(orderDTOPage)
                .build();
    }

    @Override
    public Response<List<OrderDTO>> getOrdersOfUser() {
        log.info("Fetching orders for the current user");

        User customer = userService.getCurrentLoggedInUser();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(customer);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        orderDTOs.forEach(orderItem -> {
            orderItem.setUser(null);
            orderItem.getOrderItems().forEach(item -> item.getMenu().setReviews(null)); // Avoid loading reviews in DTO
        });
        return Response.<List<OrderDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders fetched successfully")
                .data(orderDTOs)
                .build();
    }

    @Override
    public Response<OrderItemDTO> getOrderItemById(Long orderItemId) {
        log.info("Fetching order item by ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item not found with ID: " + orderItemId));

        OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
        orderItemDTO.setMenu(modelMapper.map(orderItem.getMenu(), MenuDTO.class));

        return Response.<OrderItemDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order item fetched successfully")
                .data(orderItemDTO)
                .build();
    }

    @Override
    public Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO) {
        log.info("Updating order status for order ID: {}", orderDTO.getId());

        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + orderDTO.getId()));

       OrderStatus newStatus = orderDTO.getOrderStatus();
       order.setOrderStatus(newStatus);
       orderRepository.save(order);

        return Response.<OrderDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order status updated successfully")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        log.info("Counting unique customers who have placed orders");

        long uniqueCustomerCount = orderRepository.countDistinctUsers();
        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Unique customer count fetched successfully")
                .data(uniqueCustomerCount)
                .build();
    }


    private void sendOrderConfirmationEmail(User customer, OrderDTO orderDTO) {
        String subject = "Order Confirmation - Order ID: " + orderDTO.getId();
        // create a thymeleaf template for the email body and set variables
        Context context = new Context(Locale.getDefault());

        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", orderDTO.getId());
        context.setVariable("orderDate", orderDTO.getOrderDate());
        context.setVariable("totalAmount", orderDTO.getTotalAmount());

        // format delivery address
        String deliveryAddress = orderDTO.getUser().getAddress();
        context.setVariable("deliveryAddress", deliveryAddress);
        context.setVariable("currentYear", Year.now());

        // build the order items html using StringBuilder
        StringBuilder orderItemsHtml = new StringBuilder();

        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            orderItemsHtml.append("<tr>")
                    .append("<td>").append(item.getMenu().getName()).append("</td>")
                    .append("<td>").append(item.getQuantity()).append("</td>")
                    .append("<td>").append(item.getPricePerUnit()).append("</td>")
                    .append("<td>").append(item.getSubTotal()).append("</td>")
                    .append("</tr>");

            context.setVariable("orderItemsHtml", orderItemsHtml.toString());
            context.setVariable("totalItems", orderDTO.getOrderItems().size());

            String paymentLink = basePaymentLink + orderDTO.getId() + "&amount=" + orderDTO.getTotalAmount();
            context.setVariable("paymentLink", paymentLink);

            // process the thymeleaf template to generate the email body
            String emailBody = templateEngine.process("order-confirmation", context);

            notificationService.sendEmail(NotificationDTO.builder()
                    .recipient(customer.getEmail())
                    .subject(subject)
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        }

    }
}
