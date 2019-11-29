package com.vinyl.repository;

import com.vinyl.model.CartItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends CrudRepository<CartItem, Long> {
    List <CartItem> findByCartId(Long cartId);
    CartItem deleteByItemId(Long itemId);
    //CartItem delete(CartItem cartItem);
    CartItem findByItemId(Long itemId);
}

