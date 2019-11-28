package com.vinyl.service;

import com.vinyl.model.CartItem;
import com.vinyl.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> findByOrderId(Long orderId){
        return (List<CartItem>) cartItemRepository.findByCartId(orderId);
    }

    @Override
    public CartItem findByItemId(Long itemId){
        return cartItemRepository.findByItemId(itemId);
    }

    @Override
    public void save(CartItem cartItem){
        cartItemRepository.save(cartItem);
    }
}
