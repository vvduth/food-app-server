package com.phegon.FoodApp.cart.services;

import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.auth_users.services.UserService;
import com.phegon.FoodApp.cart.dtos.CartDTO;
import com.phegon.FoodApp.cart.entity.Cart;
import com.phegon.FoodApp.cart.entity.CartItem;
import com.phegon.FoodApp.cart.repository.CartItemRepository;
import com.phegon.FoodApp.cart.repository.CartRepository;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.menu.entity.Menu;
import com.phegon.FoodApp.menu.repository.MenuRepository;
import com.phegon.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MenuRepository menuRepository;

    @Override
    public Response<?> addItemToCart(CartDTO cartDTO) {
        log.info("Adding item to cart: {}", cartDTO);

        Long menuId = cartDTO.getMenuId();
        int quantity = cartDTO.getQuantity();

        User user = userService.getCurrentLoggedInUser();

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with id: " + menuId));
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        // check if the item already exists in the cart
        Optional<CartItem> optinalCartItem = cart.getItems().stream()
                .filter(cartItem -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();
        // if present, increment the quantity
        if (optinalCartItem.isPresent()) {
            CartItem cartItem = optinalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        } else {
            // if not present, create a new CartItem
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getItems().add(newCartItem);
            cartItemRepository.save(newCartItem);

        }

        cartRepository.save(cart); // Save the cart to persist changes
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .data(modelMapper.map(cart, CartDTO.class))
                .build();
    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("Incrementing item in cart with menuId: {}", menuId);

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user with id: " + user.getId()));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found with menuId: " + menuId));

        int newQuantity = cartItem.getQuantity() + 1;

        cartItem.setQuantity(newQuantity);

        cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item incremented successfully")
                .data(modelMapper.map(cart, CartDTO.class))
                .build();
    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("Decrementing item in cart with menuId: {}", menuId);

        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user with id: " + user.getId()));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found with menuId: " + menuId));



        int newQuantity = cartItem.getQuantity() - 1;

        if (newQuantity > 0) {
            cartItem.setQuantity(newQuantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));
        } else {
            // If quantity is 0, remove the item from the cart
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item decremented successfully")
                .data(modelMapper.map(cart, CartDTO.class))
                .build();
    }

    @Override
    public Response<?> removeItem(Long itemId) {
        log.info("Removing item from cart with itemId: {}", itemId);
        User user = userService.getCurrentLoggedInUser();
        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user with id: " + user.getId()));
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found with id: " + itemId));

        if (!cart.getItems().contains(cartItem)) {
            throw new NotFoundException("Cart item with id: " + itemId + " does not belong to the user's cart");
        }
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item removed from cart successfully")
                .data(modelMapper.map(cart, CartDTO.class))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDTO> getShoppingCart() {
        log.info("Retrieving shopping cart for current user");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user with id: " + user.getId()));

        List<CartItem> cartItems = cart.getItems();

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }

        cartDTO.setTotalAmount(totalAmount);

        // remove the review from the response
        if (cartDTO.getCartItems() != null) {
            cartDTO.getCartItems().forEach(cartItem -> cartItem.getMenu().setReviews(null));
        }
        return null;
    }

    @Override
    public Response<?> clearShoppingCart() {
        return null;
    }
}
