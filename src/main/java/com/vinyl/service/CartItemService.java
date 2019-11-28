package com.vinyl.service;

import com.vinyl.model.CartItem;

import java.util.List;

public interface CartItemService {
    List<CartItem> findByOrderId(Long orderId);
    CartItem findByItemId(Long itemId);
    void save(CartItem cartItem);
}
