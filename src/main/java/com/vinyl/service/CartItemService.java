package com.vinyl.service;

import com.vinyl.model.CartItem;

import java.util.List;

public interface CartItemService {
    List<CartItem> findByCartId(Long cartId);
    void save(CartItem cartItem);
    void delete(CartItem cartItem);
}
