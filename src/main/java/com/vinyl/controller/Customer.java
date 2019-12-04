package com.vinyl.controller;

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
public class Customer {
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

    @ApiOperation(value = "Get cart details", response = Iterable.class)
    @GetMapping(value = "/customer/cart/detail")
    public ResponseEntity<?> getCart(@RequestParam String email, @RequestParam Long user_id) throws JSONException {
        if (userService.findByEmailAddress(email) != null && userService.findById(user_id) != null) {

            Cart cart = cartService.findByUserId(user_id);
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
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "Add vinyl to cart", response = Iterable.class)
    @PostMapping(value = "/vinyls/cart/{vinyl_id}")
    public @ResponseBody ResponseEntity<?> addVinyl(@Valid @RequestBody JwtVinylRequest addVinylRequest, @PathVariable Long vinyl_id){
        if(loggedIn(addVinylRequest)){
            try {
                Item item = itemService.findById(vinyl_id);
                Cart cart = cartService.findByUserId(userService.findByEmailAddress(addVinylRequest.getUsername()).getId());
                CartItem cartItem = new CartItem();


                if (addVinylRequest.getQuantity() <= 0)
                    return new ResponseEntity<>("Quantity can't be negative or zero!", HttpStatus.FORBIDDEN);
                else if (addVinylRequest.getQuantity() > item.getQuantity()) {
                    return new ResponseEntity<>("Quantity too big!", HttpStatus.FORBIDDEN);
                } else {
                    cartItem.setItem(item);
                    cartItem.setCart(cart);
                    cartItem.setQuantity(addVinylRequest.getQuantity());
                    cartItemService.save(cartItem);

                    return ResponseEntity.ok("Item added to cart!");
                }
            }
            catch (NullPointerException e){
                return new ResponseEntity<>("Quantity can't be null!", HttpStatus.FORBIDDEN);
            }
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "Remove vinyl from cart", response = Iterable.class)
    @DeleteMapping(value = "/users/{user_id}/cart/{item_id}")
    public @ResponseBody ResponseEntity<?> removeVinyl(@RequestBody JwtRequest deleteVinylRequest, @PathVariable Long user_id, @PathVariable Long item_id){
        Cart cart = cartService.findByUserId(user_id);
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

        if(loggedIn(deleteVinylRequest)){
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
    public @ResponseBody ResponseEntity<?> placeOrder(@RequestBody JwtRequest placeOrderRequest, @PathVariable Long user_id){
        if(loggedIn(placeOrderRequest)){
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
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    private Boolean passwordMatch(JwtRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }

    private Boolean passwordMatch(JwtVinylRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }

    private Boolean loggedIn(JwtRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }

    private Boolean loggedIn(JwtVinylRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }


}
