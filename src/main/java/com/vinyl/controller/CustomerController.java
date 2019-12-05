package com.vinyl.controller;

import com.vinyl.config.JwtTokenUtil;
import com.vinyl.model.*;
import com.vinyl.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value="/VinylStore/api")
@Api(value="CustomerController", description="Customer operations for the Vinyl Store")
public class CustomerController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @ApiOperation(value = "Get cart details", response = Iterable.class)
    @GetMapping(value = "/customer/cart/detail")
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String auth) throws JSONException {
        String email = jwtTokenUtil.getUsernameFromToken(auth.substring(7));

            Cart cart = cartService.findByUserId(userService.findByEmailAddress(email).getId());
            List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());
            double totalPrice = 0;

            for (int i = 0; i < (long) cartItem.size(); i++) {
                totalPrice += cartItem.get(i).getQuantity() * cartItem.get(i).getItem().getPrice();
            }

            JSONObject json = new JSONObject();
            json.put("Number of items", cartItem.size());
            json.put("Total cost", totalPrice);

            JSONArray json3 = new JSONArray();

            for (int i = 0; i < (long) cartItem.size(); i++) {
                JSONObject json2 = new JSONObject();
                json2.put("Name", cartItem.get(i).getItem().getName());
                json2.put("Description", cartItem.get(i).getItem().getDescription());
                json2.put("Price", cartItem.get(i).getItem().getPrice());
                json2.put("Quantity", cartItem.get(i).getQuantity());
                json3.put(json2);
            }

            json.put("Items", json3);


            if (cartItem.size() == 0)
                return new ResponseEntity<>("No items in cart", HttpStatus.OK);
            else return new ResponseEntity<>(json.toString(), HttpStatus.OK);

    }

    @ApiOperation(value = "Add vinyl to cart", response = Iterable.class)
    @PostMapping(value = "/vinyls/cart/{vinyl_id}")
    public @ResponseBody ResponseEntity<?> addVinyl(@RequestHeader("Authorization") String auth, @PathVariable Long vinyl_id, @RequestParam Long quantity){
        String email = jwtTokenUtil.getUsernameFromToken(auth.substring(7));

            try {
                Item item = itemService.findById(vinyl_id);
                Cart cart = cartService.findByUserId(userService.findByEmailAddress(email).getId());
                CartItem cartItem = new CartItem();


                if (quantity <= 0)
                    return new ResponseEntity<>("Quantity can't be negative or zero!", HttpStatus.FORBIDDEN);
                else if (quantity > item.getQuantity()) {
                    return new ResponseEntity<>("Quantity too big!", HttpStatus.FORBIDDEN);
                } else {
                    cartItem.setItem(item);
                    cartItem.setCart(cart);
                    cartItem.setQuantity(quantity);
                    cartItemService.save(cartItem);

                    return ResponseEntity.ok("Item added to cart!");
                }
            }
            catch (NullPointerException e){
                return new ResponseEntity<>("Quantity can't be null!", HttpStatus.FORBIDDEN);
            }
    }

    @ApiOperation(value = "Remove vinyl from cart", response = Iterable.class)
    @DeleteMapping(value = "/users/{user_id}/cart/{item_id}")
    public @ResponseBody ResponseEntity<?> removeVinyl(@RequestHeader("Authorization") String auth, @PathVariable Long user_id, @PathVariable Long item_id){
        String email = jwtTokenUtil.getUsernameFromToken(auth.substring(7));

        Cart cart = cartService.findByUserId(user_id);
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

        if(userService.findByEmailAddress(email).getId().equals(user_id)){
            cartItem.forEach(cItem -> {
                if(cItem.getItem().getId().equals(item_id))
                    cartItemService.delete(cItem);
            });

            return ResponseEntity.ok("Item deleted from cart!");
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "Place order", response = Iterable.class)
    @PutMapping(value = "/{user_id}/orders")
    public @ResponseBody ResponseEntity<?> placeOrder(@RequestHeader("Authorization") String auth, @PathVariable Long user_id){
        String email = jwtTokenUtil.getUsernameFromToken(auth.substring(7));
        if(userService.findByEmailAddress(email).getId().equals(user_id)){
            List <Boolean> quantityResponse = new ArrayList<>();
            Order order = new Order();

            Cart cart = cartService.findByUserId(user_id);
            List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

            double totalPrice = 0;

            for(int i = 0; i< (long) cartItem.size(); i++){
                totalPrice+= cartItem.get(i).getQuantity()*cartItem.get(i).getItem().getPrice();
            }


            cartItem.forEach(cItem -> {
                if(cItem.getQuantity()>cItem.getItem().getQuantity())
                    quantityResponse.add(false);
            });

            if(quantityResponse.contains(false))
                return new ResponseEntity<>("No more items availabile", HttpStatus.BAD_REQUEST);
            else{
                cartItem.forEach(cItem -> cItem.getItem().setQuantity(cItem.getItem().getQuantity()-cItem.getQuantity()));
                order.setUser(userService.findById(user_id));
                order.setCreatedAt(new Date());
                order.setUpdatedAt(new Date());
                order.setStatus(statusService.findById(1L));
                order.setTotal_price(totalPrice);
                orderService.save(order);

                return ResponseEntity.ok("Order placed!");
            }
        }
        else return new ResponseEntity<>("Your id does not correspond", HttpStatus.FORBIDDEN);
    }
}
